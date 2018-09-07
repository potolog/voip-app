package com.okawa.voip.utils

import android.annotation.SuppressLint
import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.support.annotation.DrawableRes
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

object DataBindingAdapter {

    @JvmStatic
    @BindingAdapter(value = ["bind:placeholder", "bind:image"])
    fun image(imageView: ImageView, @DrawableRes placeholder: Int?, image: Bitmap?) {
        val requestOptions = RequestOptions().dontAnimate()

        loadImage(imageView, image, placeholder, requestOptions)
    }

    @JvmStatic
    @BindingAdapter(value = ["bind:placeholder", "bind:circleImage"])
    fun circleImage(imageView: ImageView, @DrawableRes placeholder: Int?, image: Bitmap?) {
        val requestOptions = RequestOptions.circleCropTransform().dontAnimate()

        loadImage(imageView, image, placeholder, requestOptions)
    }

    /**
     * Loads the image using Glide with the provided parameters
     *
     * @param imageView where the image is going to be loaded (Uses the context as well)
     * @param image to be loaded
     * @param placeholder that will be shown when the image is not loaded
     * @param requestOptions used to define the options to load the image
     */
    @SuppressLint("CheckResult")
    private fun loadImage(imageView: ImageView, image: Bitmap?, placeholder: Int?, requestOptions: RequestOptions) {
        if(placeholder != null) {
            requestOptions.placeholder(placeholder)
        }

        val requestBuilder = if(image != null) {
            Glide.with(imageView.context).load(image)
        } else {
            Glide.with(imageView.context).load("")
        }

        requestBuilder.apply(requestOptions).into(imageView)
    }

}