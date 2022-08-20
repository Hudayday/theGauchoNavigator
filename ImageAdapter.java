package com.huday.thegauchonavigator;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter
{
    private Context   theContext;
    private Integer[]  ImageIds  =
            {

                    R.mipmap.bg3,
                    R.mipmap.bg,
                    R.mipmap.bg2,
            };
    public ImageAdapter(Context c)
    {
        theContext = c;
    }
    public int getCount()
    {
        return ImageIds.length;
    }
    public Object getItem(int position)
    {
        return position;
    }
    public long getItemId(int position)
    {
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ImageView imageView;
        imageView = new ImageView(theContext);
        imageView.setLayoutParams(new GridView.LayoutParams(500, 300)); //size of picture
        imageView.setImageResource(ImageIds[position]);
        return imageView;
    }
}