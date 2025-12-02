const {https} = require("firebase-functions/v2");
const {defineSecret} = require("firebase-functions/params");
const admin = require("firebase-admin");
const stripeLib = require("stripe");

admin.initializeApp();

const stripeSecret = defineSecret("STRIPE_SECRET");

exports.createStripePaymentLink = https.onCall(
    {secrets: [stripeSecret]},
    async (req) => {
      const stripe = stripeLib(process.env.STRIPE_SECRET);

      const {invoiceNumber, amount} = req.data;

      const paymentLink = await stripe.paymentLinks.create({
        line_items: [{
          price_data: {
            currency: "cad",
            product_data: {name: `Invoice #${invoiceNumber}`},
            unit_amount: amount,
          },
          quantity: 1,
        }],
        metadata: {invoice_number: invoiceNumber},
      });

      return paymentLink.url;
    },
);
