const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendChatNotification = onDocumentCreated(
    "chat/{messageId}",
    async (event) => {

        const snapshot = event.data;

        if (!snapshot) {
            console.log("No data");
            return;
        }

        const messageData = snapshot.data();

        const receiverId = messageData.receiverId;
        const senderName = messageData.senderName || "New Message";
        const text = messageData.message || "";yo

        const userDoc = await admin.firestore()
            .collection("users")
            .doc(receiverId)
            .get();

        if (!userDoc.exists) {
            console.log("User not found");
            return;
        }

        const token = userDoc.data().fcmToken;

        if (!token) {
            console.log("No token found");
            return;
        }

      await admin.messaging().send({
          token: token,
          data: {
              title: senderName,
              body: text
          },
          android: {
              priority: "high"
          }
      });

        console.log("Notification sent");
    }
);