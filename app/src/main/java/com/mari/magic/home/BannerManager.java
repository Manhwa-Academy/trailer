package com.mari.magic.home;

import android.content.Context;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.viewpager2.widget.ViewPager2;

import com.mari.magic.R;

import java.util.ArrayList;
import java.util.List;

public class BannerManager {

    private ViewPager2 bannerSlider;
    private LinearLayout dotsLayout;
    private List<Banner> bannerList;

    private Handler handler = new Handler();
    private Runnable runnable;

    public BannerManager(ViewPager2 slider, LinearLayout dots){
        this.bannerSlider = slider;
        this.dotsLayout = dots;
    }

    public void setup(Context context){

        bannerList = new ArrayList<>();

        bannerList.add(new Banner(
                "https://cdn.myanimelist.net/images/anime/10/47347.jpg",
                "Attack on Titan",
                "https://youtu.be/MGRm4IzK1SQ"));

        bannerList.add(new Banner(
                "https://cdn.myanimelist.net/images/anime/1286/99889.jpg",
                "Demon Slayer",
                "https://youtu.be/VQGCKyvzIM4"));

        bannerList.add(new Banner(
                "https://cdn.myanimelist.net/images/anime/1171/109222.jpg",
                "Jujutsu Kaisen",
                "https://youtu.be/pkKu9hLT-t8"));

        BannerAdapter adapter = new BannerAdapter(context,bannerList);
        bannerSlider.setAdapter(adapter);

        setupDots(context);
        autoSlide();
    }

    private void setupDots(Context context){

        dotsLayout.removeAllViews();

        ImageView[] dots = new ImageView[bannerList.size()];

        for(int i=0;i<bannerList.size();i++){

            dots[i] = new ImageView(context);
            dots[i].setImageResource(R.drawable.dot_inactive);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(20,20);

            params.setMargins(8,0,8,0);

            dotsLayout.addView(dots[i],params);
        }

        dots[0].setImageResource(R.drawable.dot_active);

        bannerSlider.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback(){

                    @Override
                    public void onPageSelected(int position){

                        for(int i=0;i<dots.length;i++){

                            dots[i].setImageResource(
                                    i==position ?
                                            R.drawable.dot_active :
                                            R.drawable.dot_inactive
                            );
                        }
                    }
                });
    }

    private void autoSlide(){

        runnable = () -> {

            int current = bannerSlider.getCurrentItem();

            if(current == bannerList.size()-1)
                bannerSlider.setCurrentItem(0);
            else
                bannerSlider.setCurrentItem(current+1);

            handler.postDelayed(runnable,4000);
        };

        handler.postDelayed(runnable,4000);
    }

    public void stop(){

        if(handler!=null && runnable!=null)
            handler.removeCallbacks(runnable);
    }
}