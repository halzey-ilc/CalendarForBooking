package com.example.testcalendar

import android.view.ViewGroup
import androidx.annotation.AnimRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.example.testcalendar.databinding.CalendarViewOptionsItemViewBinding

data class ExampleItem(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val animation: Animation,
    val createView: () -> BaseFragment,
)

data class Animation(
    @AnimRes val enter: Int,
    @AnimRes val exit: Int,
    @AnimRes val popEnter: Int,
    @AnimRes val popExit: Int,
)

val vertical = Animation(
    enter = R.anim.slide_in_up,
    exit = R.anim.fade_out,
    popEnter = R.anim.fade_in,
    popExit = R.anim.slide_out_down,
)

val horizontal = Animation(
    enter = R.anim.slide_in_right,
    exit = R.anim.slide_out_left,
    popEnter = R.anim.slide_in_left,
    popExit = R.anim.slide_out_right,
)

class CalendarViewOptionsAdapter(val onClick: (ExampleItem) -> Unit) :
    RecyclerView.Adapter<CalendarViewOptionsAdapter.HomeOptionsViewHolder>() {

    val examples = listOf(
        ExampleItem(
            R.string.example_1_title,
            R.string.example_1_subtitle,
            vertical,
        ) { CalendarFragment() },
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeOptionsViewHolder {
        return HomeOptionsViewHolder(
            CalendarViewOptionsItemViewBinding.inflate(
                parent.context.layoutInflater,
                parent,
                false,
            ),
        )
    }

    override fun onBindViewHolder(viewHolder: HomeOptionsViewHolder, position: Int) {
        viewHolder.bind(examples[position])
    }

    override fun getItemCount(): Int = examples.size

    inner class HomeOptionsViewHolder(private val binding: CalendarViewOptionsItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                onClick(examples[bindingAdapterPosition])
            }
        }

        fun bind(item: ExampleItem) {
            val context = itemView.context
            binding.itemOptionTitle.text = context.getString(item.titleRes)
            binding.itemOptionSubtitle.text = context.getString(item.subtitleRes)
        }
    }
}
