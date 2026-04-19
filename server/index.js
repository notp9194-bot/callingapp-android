const express = require('express');
const cors    = require('cors');
const admin   = require('firebase-admin');

const app = express();
app.use(cors({ origin: '*' }));
app.use(express.json());

let serviceAccount;
try {
  serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT);
} catch(e) {
  console.error('❌ FIREBASE_SERVICE_ACCOUNT env variable set nahi hai!');
  process.exit(1);
}

admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });
console.log('✅ Firebase Admin initialized');

app.post('/send-notification', async (req, res) => {
  const { token, title, body, data } = req.body;
  if (!token || !title) return res.status(400).json({ error: 'token aur title required' });
  try {
    const result = await admin.messaging().send({
      token,
      notification: { title, body: body || '' },
      data: data || {},
      android: {
        priority: 'high',
        notification: {
          channelId: 'incoming_calls',
          priority: 'max',
          sound: 'default',
          vibrateTimingsMillis: ['0','400','150','400','150','400'],
          visibility: 'PUBLIC'
        }
      }
    });
    console.log('[FCM] Sent:', result);
    res.json({ success: true, messageId: result });
  } catch (err) {
    console.error('[FCM] Error:', err.message);
    res.status(500).json({ error: err.message });
  }
});

app.get('/', (req, res) => {
  res.json({ status: '✅ CallingApp FCM Server running' });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`🚀 Server on port ${PORT}`));
