package me.vkarmane.sdksample

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 *  @author Sergei Solodkov
 */
class Adapter(
    private val clickAction: ItemClickAction<Item>
) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    var data: List<Item> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_list, parent, false))
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.itemView.setOnClickListener {
            clickAction.invoke(item)
        }
        holder.textView.text = item.text
        if (item.isChecked){
            holder.checkView.visibility = View.VISIBLE
        } else {
            holder.checkView.visibility = View.GONE
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView:TextView = view.findViewById(R.id.itemText)
        val checkView: View = view.findViewById(R.id.itemCheck)
    }
}

typealias ItemClickAction<T> = (T) -> Unit
