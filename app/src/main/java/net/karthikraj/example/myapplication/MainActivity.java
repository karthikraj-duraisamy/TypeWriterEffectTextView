package net.karthikraj.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewDebug;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by Karthikraj on 2/1/18.
 */

public class MainActivity extends AppCompatActivity {


        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

            int padding = (int)(16 * getResources().getDisplayMetrics().density);

            final TypeWriterEffectTextView textView = new TypeWriterEffectTextView(this);
            textView.setPadding(padding, padding, padding, padding);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22.0f);
            textView.setTextColor(Color.GREEN);
            textView.setTypeface(Typeface.MONOSPACE);

            setContentView(textView);

            textView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    textView.setText(getString(R.string.printing_text));
                }
            });
        }

        private static class TypeWriterEffectTextView extends AppCompatTextView
        {
            private Interpolator mInterpolator;
            private long mStart, mDurationPerLetter;
            private boolean mAnimating = false;

            private SpannableString mFadeyText;
            private CharSequence mText;


            public TypeWriterEffectTextView(Context context)
            {
                super(context);
                initView();
            }

            public TypeWriterEffectTextView(Context context, AttributeSet attrs)
            {
                super(context, attrs);
                initView();
            }

            public TypeWriterEffectTextView(Context context, AttributeSet attrs, int defStyle)
            {
                super(context, attrs, defStyle);
                initView();
            }

            private void initView()
            {
                // Set defaults
                mInterpolator = new DecelerateInterpolator();
                mDurationPerLetter = 250;
            }

            public void setInterpolator(Interpolator interpolator)
            {
                mInterpolator = interpolator;
            }

            public void setDurationPerLetter(long durationPerLetter)
            {
                mDurationPerLetter = durationPerLetter;
            }

            @Override
            public void setText(CharSequence text, BufferType type)
            {
                mText = text;

                mFadeyText = new SpannableString(text);

                TypeWritterLetterSpan[] letters = mFadeyText.getSpans(0, mFadeyText.length(), TypeWritterLetterSpan.class);
                for(TypeWritterLetterSpan letter : letters){
                    mFadeyText.removeSpan(letter);
                }

                final int length = mFadeyText.length();
                for(int i = 0; i < length; i++){
                    mFadeyText.setSpan(new TypeWritterLetterSpan(), i, i + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }

                super.setText(mFadeyText, BufferType.SPANNABLE);

                mAnimating = true;
                mStart = AnimationUtils.currentAnimationTimeMillis();
                ViewCompat.postInvalidateOnAnimation(this);
            }

            @Override
            @ViewDebug.CapturedViewProperty
            public CharSequence getText()
            {
                return mText;
            }

            public boolean isAnimating()
            {
                return mAnimating;
            }

            @Override
            protected void onDraw(Canvas canvas)
            {
                super.onDraw(canvas);

                if(mAnimating){
                    long mDelta   = AnimationUtils.currentAnimationTimeMillis() - mStart;

                    TypeWritterLetterSpan[] letters = mFadeyText.getSpans(0, mFadeyText.length(), TypeWritterLetterSpan.class);
                    final int length = letters.length;
                    for(int i = 0; i < length; i++){
                        TypeWritterLetterSpan letter = letters[i];
                        float delta = (float)Math.max(Math.min((mDelta - (i * mDurationPerLetter)), mDurationPerLetter), 0);
                        letter.setAlpha(mInterpolator.getInterpolation(delta / (float)mDurationPerLetter));
                    }
                    if(mDelta < mDurationPerLetter * length){
                        ViewCompat.postInvalidateOnAnimation(this);
                    }else{
                        mAnimating = false;
                    }
                }
            }


            private class TypeWritterLetterSpan extends CharacterStyle implements UpdateAppearance
            {
                private float mAlpha = 0.0f;


                public void setAlpha(float alpha)
                {
                    mAlpha = Math.max(Math.min(alpha, 1.0f), 0.0f);
                }

                @Override
                public void updateDrawState(TextPaint tp)
                {
                    int color = ((int)(0xFF * mAlpha) << 24) | (tp.getColor() & 0x00FFFFFF);
                    tp.setColor(color);
                }
            }
        }
}
