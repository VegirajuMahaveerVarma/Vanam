# Trains the photo classifier. Run in Colab/Kaggle: python train_classifier.py data/
# data/ has one folder per species id (e.g. data/neem/*.jpg, data/peepal/*.jpg). Aim for 100+ photos per species.
import sys, tensorflow as tf
train, val = tf.keras.utils.image_dataset_from_directory(sys.argv[1], image_size=(224, 224), batch_size=32,
                                                         validation_split=0.2, subset="both", seed=1)
names = train.class_names
base = tf.keras.applications.MobileNetV2(input_shape=(224, 224, 3), include_top=False, weights="imagenet", pooling="avg")
base.trainable = False
m = tf.keras.Sequential([tf.keras.layers.Rescaling(1 / 127.5, offset=-1), base, tf.keras.layers.Dropout(0.2),
                         tf.keras.layers.Dense(len(names), activation="softmax")])
m.compile("adam", "sparse_categorical_crossentropy", ["accuracy"])
m.fit(train, validation_data=val, epochs=8)
def rep():
    for x, _ in train.take(20):
        yield [x[:1]]
c = tf.lite.TFLiteConverter.from_keras_model(m)
c.optimizations = [tf.lite.Optimize.DEFAULT]; c.representative_dataset = rep
c.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
c.inference_input_type = tf.uint8; c.inference_output_type = tf.uint8
open("species_classifier.tflite", "wb").write(c.convert())
open("species_labels.txt", "w").write("\n".join(names))
print("Done. Copy both files into app/src/main/assets/")
