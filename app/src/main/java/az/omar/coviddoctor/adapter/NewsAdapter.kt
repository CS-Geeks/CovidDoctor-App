package az.omar.coviddoctor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import az.omar.coviddoctor.R
import az.omar.coviddoctor.pojo.Article
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_article.view.*

class NewsAdapter:
    ListAdapter<Article, NewsAdapter.NewsViewHolder>(diffUtilCallback()) {


    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentArticle = getItem(position)
        // use apply to apply all what is inside to the outer object
        // relief from the hell of holder. holder. holder.
        val requestOptions = RequestOptions().placeholder(R.drawable.ic_no_image)
        holder.itemView.apply {
            // this is the view not the adapter
            Glide.with(this).setDefaultRequestOptions(requestOptions)
                .load(currentArticle.url).into(iv_article_image)

            tv_article_name.text = currentArticle.name

            setOnClickListener { onProductBodyClickListener?.let { it(currentArticle) } }
        }
    }

}

private var onProductBodyClickListener: ((Article) -> Unit)? = null

fun setOnProductBodyClickListener(listener: (Article) -> Unit) {
    onProductBodyClickListener = listener
}


fun diffUtilCallback(): ItemCallback<Article> = object : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article) =
        oldItem.name + oldItem.url == newItem.name + newItem.url

    override fun areContentsTheSame(oldItem: Article, newItem: Article) = oldItem == newItem

}

