const {https} = require("firebase-functions/v2");
const {defineSecret} = require("firebase-functions/params");
const admin = require("firebase-admin");
const stripeLib = require("stripe");

admin.initializeApp();

const stripeSecret = defineSecret("STRIPE_SECRET");
const stripeWebhookSecret = defineSecret("STRIPE_WEBHOOK_SECRET");

// ✅ 1. PAYMENT LINK (add companyId + invoiceId metadata)
exports.createStripePaymentLink = https.onCall(
    {secrets: [stripeSecret]},
    async (req) => {
      const stripe = stripeLib(process.env.STRIPE_SECRET);
      const {invoiceNumber, amount, companyId, invoiceId} = req.data;

      const paymentLink = await stripe.paymentLinks.create({
        line_items: [{
          price_data: {
            currency: "cad",
            product_data: {name: `Invoice #${invoiceNumber}`},
            unit_amount: amount,
          },
          quantity: 1,
        }],
        metadata: {
          invoice_number: invoiceNumber,
          invoice_id: invoiceId.toString(),
          company_id: companyId.toString(),
        },
      });

      return paymentLink.url;
    },
);

// ✅ 2. WEBHOOK (creates Receipt using YOUR fields)
exports.stripeWebhook = https.onRequest(
    {secrets: [stripeSecret, stripeWebhookSecret]},
    async (req, res) => {
      if (req.method !== "POST") {
        return res.status(405).send("Method Not Allowed");
      }

      const sig = req.headers["stripe-signature"];
      let event;

      try {
        event = stripeLib(process.env.STRIPE_WEBHOOK_SECRET)
            .webhooks.constructEvent(
                req.rawBody,
                sig,
                process.env.STRIPE_WEBHOOK_SECRET,
            );
      } catch (err) {
        console.error("Webhook signature failed:", err.message);
        return res.status(400).send("Webhook signature failed");
      }

      if (event.type === "checkout.session.completed") {
        const session = event.data.object;
        const invoiceNumber = session.metadata.invoice_number;
        const invoiceId = parseInt(session.metadata.invoice_id || "0");
        const companyId = parseInt(session.metadata.company_id || "0");

        if (invoiceNumber && invoiceId && companyId) {
          const db = admin.database();
          const companyRootRef =
            db.ref(`companies/${companyId}/data`);
          const counterRef = companyRootRef.child("receiptCounter");
          const receiptsRef = companyRootRef.child("receipts");

          try {
            const nextId = await counterRef.transaction((current) => {
              if (current === null || current < 700000) {
                return 700001;
              }
              return current + 1;
            }).then((result) => {
              if (!result.committed) {
                throw new Error("Counter transaction not committed");
              }
              return result.snapshot.val();
            });

            await receiptsRef.push({
              id: nextId,
              receiptNumber: `AUTO_REC_${invoiceNumber}`,
              receiptDate: admin.database.ServerValue.TIMESTAMP,
              invoiceId: invoiceId,
              lastModified: admin.database.ServerValue.TIMESTAMP,
              isDeleted: false,
            });

            console.log(
                `✅ Receipt ${nextId} AUTO_REC_${invoiceNumber} created ` +
                `for invoice ${invoiceId} (company ${companyId})`,
            );
          } catch (e) {
            console.error("Failed to create auto receipt:", e);
          }
        }
      }

      res.status(200).json({received: true});
    },
);
