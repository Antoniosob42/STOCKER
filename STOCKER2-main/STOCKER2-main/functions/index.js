const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.notifyFavoriteProductInStock = functions.firestore
    .document('Productos/{productId}')
    .onUpdate((change, context) => {
        const productId = context.params.productId;
        const newValue = change.after.data();
        const previousValue = change.before.data();

        if (previousValue.cantidadStock === 0 && newValue.cantidadStock > 0) {
            return admin.firestore().collection('usuarios').get().then(snapshot => {
                const promises = [];
                snapshot.forEach(userDoc => {
                    const userData = userDoc.data();
                    if (userData.favoritos && userData.favoritos.includes(productId)) {
                        const payload = {
                            notification: {
                                title: 'Producto en Stock',
                                body: 'El producto que tenias en favoritos está en stock!'
                            }
                        };
                        const token = userData.fcmToken;
                        if (token) {
                            promises.push(admin.messaging().sendToDevice(token, payload));
                        }
                    }
                });
                return Promise.all(promises);
            });
        } else {
            return null;
        }
    });
