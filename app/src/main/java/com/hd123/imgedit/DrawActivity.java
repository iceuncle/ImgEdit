package com.hd123.imgedit;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.hd123.imgedit.bean.DrawType;

public class DrawActivity extends AppCompatActivity implements View.OnClickListener {
    private DrawView drawView;
    private EditText editText;
    private LinearLayout editView;

    private TextView drawBtn, textBtn, mosaicBtn, confirmBtn, lastBtn, nextBtn;

    private boolean isDrawMode = false, isTextMode = false, isMosaicMode = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.choose_color) {
            ColorPickerDialogBuilder
                    .with(this)
                    .setTitle("选择颜色")
                    .initialColor(Color.RED)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setPositiveButton("ok", new ColorPickerClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                            drawView.setPaintColor(selectedColor);
                            drawView.setTextColor(selectedColor);
                        }
                    })
                    .build()
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawView = (DrawView) findViewById(R.id.draw_view);
        editText = (EditText) findViewById(R.id.editText);
        editView = (LinearLayout) findViewById(R.id.edit_view);


        lastBtn = (TextView) findViewById(R.id.last_btn);
        nextBtn = (TextView) findViewById(R.id.next_btn);
        drawBtn = (TextView) findViewById(R.id.draw_btn);
        textBtn = (TextView) findViewById(R.id.text_btn);
        mosaicBtn = (TextView) findViewById(R.id.mosaic_btn);
        confirmBtn = (TextView) findViewById(R.id.confirm_btn);

        lastBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        drawBtn.setOnClickListener(this);
        textBtn.setOnClickListener(this);
        mosaicBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);

        drawView.setEditText(editText);
        drawView.setEditView(editView);
        drawView.setPaintColor(Color.RED);
        drawView.setTextColor(Color.RED);
        drawView.setSuperMode(true);

        String picPath = getIntent().getStringExtra("picPath");
        Bitmap picture = BitmapFactory.decodeFile(picPath);

        drawView.setBasePicture(picture);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                drawView.setText(text);
            }
        });


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.last_btn:
                drawView.lastStep();
                break;
            case R.id.next_btn:
                drawView.nextStep();
                break;
            case R.id.draw_btn:
                if (isDrawMode) {
                    isDrawMode = false;
                    drawView.setDrawType(DrawType.NORMAL);
                    resetButtonColor();
                } else {
                    isDrawMode = true;
                    isTextMode = false;
                    isMosaicMode = false;
                    drawView.setDrawType(DrawType.DRAW);
                    drawView.confirmText();

                    drawBtn.setTextColor(getResources().getColor(R.color.grey));
                    textBtn.setTextColor(Color.WHITE);
                    mosaicBtn.setTextColor(Color.WHITE);
                }

                break;
            case R.id.text_btn:
                if (isTextMode) {
                    isTextMode = false;
                    drawView.confirmText();
                    resetButtonColor();
                    drawView.setDrawType(DrawType.NORMAL);
                } else {
                    isTextMode = true;
                    isDrawMode = false;
                    isMosaicMode = false;
                    drawView.setDrawType(DrawType.TEXT);
                    textBtn.setTextColor(getResources().getColor(R.color.grey));
                    drawBtn.setTextColor(Color.WHITE);
                    mosaicBtn.setTextColor(Color.WHITE);
                }

                break;
            case R.id.mosaic_btn:
                if (isMosaicMode) {
                    isMosaicMode = false;
                    resetButtonColor();
                    drawView.setDrawType(DrawType.NORMAL);

                } else {
                    isMosaicMode = true;
                    isDrawMode = false;
                    isTextMode = false;
                    drawView.setDrawType(DrawType.MOSAIC);
                    drawView.confirmText();

                    mosaicBtn.setTextColor(getResources().getColor(R.color.grey));
                    drawBtn.setTextColor(Color.WHITE);
                    textBtn.setTextColor(Color.WHITE);
                }

                break;
            case R.id.confirm_btn:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                break;
        }
    }


    public void resetButtonColor() {
        drawBtn.setTextColor(Color.WHITE);
        textBtn.setTextColor(Color.WHITE);
        mosaicBtn.setTextColor(Color.WHITE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        drawView.destory();
    }
}
