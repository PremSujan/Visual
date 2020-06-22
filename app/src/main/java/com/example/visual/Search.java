package com.example.visual;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import com.microsoft.azure.cognitiveservices.search.imagesearch.BingImageSearchAPI;
import com.microsoft.azure.cognitiveservices.search.imagesearch.BingImageSearchManager;
import com.microsoft.azure.cognitiveservices.search.imagesearch.models.ImageObject;
import com.microsoft.azure.cognitiveservices.search.imagesearch.models.ImagesModel;

import okhttp3.Callback;

public class Search extends AppCompatActivity {
    // presets for rgb conversion
    private static final int RESULTS_TO_SHOW = 3;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;

    private int DIM_IMG_SIZE_X = 299;
    private int DIM_IMG_SIZE_Y = 299;
    private int DIM_PIXEL_SIZE = 3;

    private Interpreter tensorflowlite;
    private final Interpreter.Options tensorflowlite_option =new Interpreter.Options();
    private List<String> keys_list;

    private ByteBuffer Pic_data = null;
    private int[] intValues;

    private float[][] keys_prob_array = null;
    private String[] high_prob_keys = null;
    private String[] high_probs = null;

    private LinearLayout ranks;
    private ImageView selected_image;
    private Button search_button;
    private Button back_button;
    private Button key1_text;
    private Button key2_text;
    private Button key3_text;

    private TextView prob1_text;
    private TextView prob2_text;
    private TextView prob3_text;

    private PriorityQueue<Map.Entry<String, Float>> keys_inorder =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.v("Search","Inside search java file");
        intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

        super.onCreate(savedInstanceState);

        //initialise model and keys
        try{
            tensorflowlite = new Interpreter(load_model(),tensorflowlite_option);
            keys_list =load_keys();
        } catch (Exception ex){
            ex.printStackTrace();
        }

        Pic_data = ByteBuffer.allocateDirect(4*DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y *DIM_PIXEL_SIZE);
        Pic_data.order(ByteOrder.nativeOrder());
        keys_prob_array =new float[1][keys_list.size()];
        setContentView(R.layout.search);
        Log.v("Search","Search Content set");

        ranks = (LinearLayout)findViewById(R.id.labels);
        key1_text = (Button)findViewById(R.id.key1);
        key1_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i1 = new Intent(v.getContext(),image_result_list.class);
                i1.putExtra("keyword",key1_text.getText().toString());
                startActivity(i1);
            }
        });
        key2_text = (Button)findViewById(R.id.key2);
        key2_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i2 = new Intent(v.getContext(),image_result_list.class);
                i2.putExtra("keyword",key2_text.getText().toString());
                startActivity(i2);
            }
        });
        key3_text = (Button)findViewById(R.id.key3);
        key3_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i3 = new Intent(v.getContext(),image_result_list.class);
                i3.putExtra("keyword",key3_text.getText().toString());
                startActivity(i3);
            }
        });

        prob1_text = findViewById(R.id.prob1);
        prob2_text = findViewById(R.id.prob2);
        prob3_text = findViewById(R.id.prob3);

        final ImageView selected_image = findViewById(R.id.selected_image);
        //Uri recieved_uri = (Uri)getIntent().getParcelableExtra("resID_uri");

        byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), recieved_uri);
        selected_image.setImageBitmap(bmp);

        search_button = findViewById(R.id.search_image);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ranks.setVisibility(View.VISIBLE);
                Bitmap org_bitmap = ((BitmapDrawable)selected_image.getDrawable()).getBitmap();
                Bitmap sized_bitmap = Resize_bitmap(org_bitmap,DIM_IMG_SIZE_X,DIM_IMG_SIZE_Y);
                Bitmap_to_Buffer(sized_bitmap,Pic_data);
                tensorflowlite.run(Pic_data,keys_prob_array);
                show_high_prob_keys();
            }
        });

        back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent back = new Intent(Search.this, MainActivity.class);
                startActivity(back);
            }
        });

        high_prob_keys = new String[RESULTS_TO_SHOW];
        high_probs = new String[RESULTS_TO_SHOW];


    }


    private MappedByteBuffer load_model() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("inception_float.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> load_keys() throws IOException {
        List<String> labelList = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(this.getAssets().open("labels.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    public Bitmap Resize_bitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    private void Bitmap_to_Buffer(Bitmap bitmap,ByteBuffer img_data) {
        if (img_data == null) {
            return;
        }
        img_data.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // loop through all pixels
        int pixel = 0;
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                // get rgb values from intValues where each int holds the rgb values for a pixel.
                // if quantized, convert each rgb value to a byte, otherwise to a float
                    img_data.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                    img_data.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                    img_data.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);

            }
        }
    }

    private void show_high_prob_keys() {// add all results to priority queue
        for (int i = 0; i < keys_list.size(); ++i) {
            keys_inorder.add(new AbstractMap.SimpleEntry<>(keys_list.get(i), keys_prob_array[0][i]));
            if (keys_inorder.size() > RESULTS_TO_SHOW) {
                keys_inorder.poll();
            }
        }

        // get top results from priority queue
        final int size = keys_inorder.size();
        for (int i = 0; i < size; ++i) {
            Map.Entry<String, Float> label = keys_inorder.poll();
            high_prob_keys[i] = label.getKey();
            high_probs[i] = String.format("%.0f%%",label.getValue()*100);
        }

        // set the corresponding textviews with the results
        key1_text.setText("1. "+high_prob_keys[2]);
        key2_text.setText("2. "+high_prob_keys[1]);
        key3_text.setText("3. "+high_prob_keys[0]);
        prob1_text.setText(high_probs[2]);
        prob2_text.setText(high_probs[1]);
        prob3_text.setText(high_probs[0]);
    }



}
