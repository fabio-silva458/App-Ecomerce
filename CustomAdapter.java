package com.example.admin.jprod;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    ArrayList<ProductList> productLists = new ArrayList<ProductList>();
    HashMap<String, List<String>> hashlist;
    Context context;
    String appurl;

    public CustomAdapter(Context context, ArrayList<ProductList> productLists, HashMap<String, List<String>> hmaplist, String appurl) {
        this.context = context;
        this.productLists = productLists;
        this.hashlist=hmaplist;
        this.appurl = appurl;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rowlayout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        // set the data in items
        System.out.println("onBindViewHolder");
        holder.name.setText(productLists.get(position).getProdname());
        System.out.println(productLists.get(position).getProdname());
        String mainimg="";
        if(hashlist.containsKey(productLists.get(position).getProdno())){
            if(hashlist.get(productLists.get(position).getProdno()).size()!=0) {
                List<String> prodimglist = hashlist.get(productLists.get(position).getProdno());
                mainimg = prodimglist.get(0);
            }
        }
        System.out.println("mainimg="+mainimg);
//        holder.image.setImageResource(productLists.get(position).getProdimage());
        //holder.image.setImageResource(personImages.get(position));
//        Glide.with(context).load(appurl + "/DisplayImage?picname=P_tyuu.png")
        if(mainimg!=null) {
            System.out.println(" mainimgurl= "+appurl + "/DisplayImage?type=prod&picname=" + mainimg);
            Glide.with(context).load(appurl + "/DisplayImage?type=prod&picname=" + mainimg)
                    .placeholder(R.drawable.image)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .error(R.drawable.image)
                    .into(holder.image);
        }else{
            holder.image.setImageResource(R.drawable.image);
        }
        // implement setOnClickListener event on item view.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open another activity on item click
//                Intent intent = new Intent(context, SecondActivity.class);
//                intent.putExtra("image", personImages.get(position)); // put image data in Intent
//                context.startActivity(intent); // start Intent
//                Toast.makeText(context, "Item" + productLists.get(position).getProdname(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, ProductDetail.class);
                intent.putExtra("pno", productLists.get(position).getProdno());
                intent.putExtra("pname", productLists.get(position).getProdname());

//                intent.putExtra("image", personImages.get(position)); // put image data in Intent
                context.startActivity(intent); // start Intent
            }
        });

    }


    @Override
    public int getItemCount() {
        return productLists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        // init the item view's
        TextView name;
        ImageView image;

        public MyViewHolder(View itemView) {
            super(itemView);

            // get the reference of item view's
            name = (TextView) itemView.findViewById(R.id.name);
            image = (ImageView) itemView.findViewById(R.id.image);

        }
    }
}
