package com.example.glideimagetest

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.GlideSubcomposition
import com.bumptech.glide.integration.compose.RequestState
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.glideimagetest.ui.theme.GlideImageTestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GlideImageTestTheme {
                    GlideImageText()
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GlideImageText() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val imageUrl = "https://picsum.photos/seed/picsum/200/300"
        // 簡易範例
        Text(
            text = "簡易範例",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        GlideImage(
            model = imageUrl,
            contentDescription = "Picture of cat"
        )

        // placeholder示範
        // placeholder()：Glide Compose 提供的函式，可用 Drawable、Resource ID 或 Painter
        Text(
            text = "placeholder示範",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        GlideImage(
            model = "https://via.placeholder.com/2000x3000?text=Large+Image",
            contentDescription = "Image with placeholder",
            modifier = Modifier.size(200.dp),
            // loading：載入中所顯示的 Placeholder
            loading = placeholder(ColorPainter(Color.LightGray)),
            // failure：載入失敗時所顯示的 Placeholder
            failure = placeholder(R.drawable.baseline_error_24),
        )

        // requestBuilderTransform 自訂範例
        /**
         * 1. thumbnail(...) 的目的是提供「先載入略縮圖，再載入正式圖」的功能。也就是說，當主圖（高畫質圖）尚未載入完成前，先顯示一個較小（低畫質）的圖片，讓使用者有初步的視覺反饋。
         * 2. asDrawable(): 指定要產生的是 Drawable 型態的載入結果。也可以使用asBitmap(), asGif(), 或 asDrawable() 等方法來決定載入後的資源型態
         * 3. dontAnimate(): 停用 Glide 預設的圖片載入動畫，在某些情況下會有淡入 (fade in) 等小幅度的動畫效果
         * 4. RequestManager: 管理與協調圖片載入請求，並與生命周期綁定，避免資源洩漏
         */
        // 1. 取得目前的 Context
        val context = LocalContext.current

        // 2. 建立 (或取得) RequestManager 的實例
        //    一般只需要建立一次，故建議用 remember 做快取。
        val requestManager = remember {
            com.bumptech.glide.Glide.with(context)
        }
        Text(
            text = "requestBuilderTransform 自訂範例",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        GlideImage(
            model = imageUrl,
            contentDescription = "Image with custom requestBuilderTransform",
            modifier = Modifier.size(300.dp),
            alignment = Alignment.TopStart,
            // requestBuilderTransform：可在這裡客製化 Glide Request
            requestBuilderTransform = { requestBuilder: RequestBuilder<Drawable> ->
                // 例如：加入一個 thumbnail 請求
                requestBuilder
                    .thumbnail(
                        requestManager
                            .asDrawable()
                            .load(imageUrl)
                            .override(50, 50) // 只載入 50x50 略縮圖
                    )
//                    .dontAnimate() // 進用預設動畫
            }
        )
        // Transition（CrossFade）示範
        Text(
            text = "Transition（CrossFade）示範",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        GlideImage(
            model = imageUrl,
            contentDescription = "Image with cross fade transition",
            modifier = Modifier.size(300.dp),
            alignment = Alignment.TopStart,
            // Compose 內建的 CrossFade Transition
            transition = CrossFade
        )

        // Subcomposition（監測載入狀態）
        Text(
            text = "Subcomposition（監測載入狀態）",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        GlideSubcomposition(
            model = imageUrl,
            modifier = Modifier.size(200.dp),
            // 如果需要在這裡自訂 requestBuilder，可以使用 requestBuilderTransform
            requestBuilderTransform = {
                it.dontAnimate()
            }
        ) {
            // 透過 'state' 動態顯示不同的 Composable
            when (state) {
                is RequestState.Loading -> {
                    Log.d("imageLoading", "Loading...")
                    Box {
                        CircularProgressIndicator()
                    }
                }
                is RequestState.Failure -> {
                    // 顯示一個錯誤圖示或錯誤訊息
                    Box {
                        placeholder(R.drawable.baseline_error_24)
                    }
                }
                is RequestState.Success -> {
                    // 'painter' 來自 GlideSubcompositionScope
                    androidx.compose.foundation.Image(
                        painter = painter,
                        contentDescription = "Subcomposed image"
                    )
                }
            }
        }

        // 快取方式設定
        Text(
            text = "快取方式設定",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        GlideImage(
            model = imageUrl,
            contentDescription = "Image with custom cache strategy",
            modifier = Modifier.size(200.dp),
            alignment = Alignment.TopStart,
            // 使用 requestBuilderTransform 客製化 Glide 請求
            requestBuilderTransform = { requestBuilder ->
                // 在這裡設定快取策略
                requestBuilder
                    // 同時快取「原始圖片」與「最終轉換後」圖片
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    // 是否跳過記憶體快取，false 代表要使用記憶體快取
                    .skipMemoryCache(false)
            }
        )
    }



}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GlideImageTestTheme {
        GlideImageText()
    }
}