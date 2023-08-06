package com.example.testcalendar

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.children
import com.example.testcalendar.ContinuousSelectionHelper.getSelection
import com.example.testcalendar.ContinuousSelectionHelper.isInDateBetweenSelection
import com.example.testcalendar.ContinuousSelectionHelper.isOutDateBetweenSelection
import com.example.testcalendar.databinding.DayCalendarBinding
import com.example.testcalendar.databinding.FragmentCalendarBinding
import com.example.testcalendar.databinding.HeaderCalendarBinding
import com.example.testcalendar.shared.displayText
import com.google.android.material.snackbar.Snackbar
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter


class CalendarFragment : BaseFragment(R.layout.fragment_calendar), HasToolbar, HasBackButton {

    override val toolbar: Toolbar
        get() = binding.exFourToolbar

    override val titleRes: Int? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private val today = LocalDate.now()

    @RequiresApi(Build.VERSION_CODES.O)
    private var selection = DateSelection()

    @RequiresApi(Build.VERSION_CODES.O)
    private val headerDateFormatter = DateTimeFormatter.ofPattern("EEE'\n'd MMM")

    private lateinit var binding: FragmentCalendarBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addStatusBarColorUpdate(R.color.white)
        setHasOptionsMenu(true)
        binding = FragmentCalendarBinding.bind(view)
        val daysOfWeek = daysOfWeek()
        binding.legendLayout.root.children.forEachIndexed { index, child ->
            (child as TextView).apply {
                text = daysOfWeek[index].displayText()
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setTextColorRes(R.color.example_4_grey)
            }
        }

        configureBinders()
        val currentMonth = YearMonth.now()
        binding.exFourCalendar.setup(
            currentMonth,
            currentMonth.plusMonths(12),
            daysOfWeek.first(),
        )
        binding.exFourCalendar.scrollToMonth(currentMonth)

        binding.exFourSaveButton.setOnClickListener click@{
            val (startDate, endDate) = selection
            if (startDate != null && endDate != null) {
                val text = dateRangeDisplayText(startDate, endDate)
                Snackbar.make(requireView(), text, Snackbar.LENGTH_LONG).show()
            }
            parentFragmentManager.popBackStack()
        }

        bindSummaryViews()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun bindSummaryViews() {
        binding.exFourStartDateText.apply {
            if (selection.startDate != null) {
                text = headerDateFormatter.format(selection.startDate)
                setTextColorRes(R.color.example_4_grey)
            } else {
                text = getString(R.string.start_date)
                setTextColor(Color.GRAY)
            }
        }

        binding.exFourEndDateText.apply {
            if (selection.endDate != null) {
                text = headerDateFormatter.format(selection.endDate)
                setTextColorRes(R.color.example_4_grey)
            } else {
                text = getString(R.string.end_date)
                setTextColor(Color.GRAY)
            }
        }

        binding.exFourSaveButton.isEnabled = selection.daysBetween != null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.calendar_menu, menu)
        binding.exFourToolbar.post {
            binding.exFourToolbar.findViewById<TextView>(R.id.menuItemClear).apply {
                setTextColor(requireContext().getColorCompat(R.color.example_4_grey))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                isAllCaps = false
            }
        }
        menu.findItem(R.id.menuItemClear).setOnMenuItemClickListener {
            selection = DateSelection()
            binding.exFourCalendar.notifyCalendarChanged()
            bindSummaryViews()
            true
        }
    }

    override fun onStart() {
        super.onStart()
        val closeIndicator = requireContext().getDrawableCompat(R.drawable.ic_close).apply {
            colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                requireContext().getColorCompat(R.color.example_4_grey),
                BlendModeCompat.SRC_ATOP,
            )
        }
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(closeIndicator)
    }

    private fun configureBinders() {
        val clipLevelHalf = 5000
        val ctx = requireContext()
        val rangeStartBackground =
            ctx.getDrawableCompat(R.drawable.continuous_selected_bg_start).also {
                it.level = clipLevelHalf
            }
        val rangeEndBackground =
            ctx.getDrawableCompat(R.drawable.continuous_selected_bg_end).also {
                it.level = clipLevelHalf
            }
        val rangeMiddleBackground =
            ctx.getDrawableCompat(R.drawable.continuous_selected_bg_middle)
        val singleBackground = ctx.getDrawableCompat(R.drawable.single_selected_bg)
        val todayBackground = ctx.getDrawableCompat(R.drawable.today_bg)

        @RequiresApi(Build.VERSION_CODES.O)
        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val binding = DayCalendarBinding.bind(view)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate &&
                        (day.date == today || day.date.isAfter(today))
                    ) {
                        selection = getSelection(
                            clickedDate = day.date,
                            dateSelection = selection,
                        )
                        this@CalendarFragment.binding.exFourCalendar.notifyCalendarChanged()
                        bindSummaryViews()
                    }
                }
            }
        }

        binding.exFourCalendar.dayBinder = object : MonthDayBinder<DayViewContainer> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun create(view: View) = DayViewContainer(view)

            @RequiresApi(Build.VERSION_CODES.O)



            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val textView = container.binding.exFourDayText
                val roundBgView = container.binding.exFourRoundBackgroundView
                val continuousBgView = container.binding.exFourContinuousBackgroundView

                textView.text = null
                roundBgView.makeInVisible()
                continuousBgView.makeInVisible()

                val (startDate, endDate) = selection

                when (data.position) {
                    DayPosition.MonthDate -> {
                        textView.text = data.date.dayOfMonth.toString()
                        if (data.date.isBefore(today)) {
                            textView.setTextColorRes(R.color.example_4_grey_past)
                        } else {
                            when {
                                startDate == data.date && endDate == null -> {
                                    textView.setTextColorRes(R.color.white)
                                    roundBgView.applyBackground(singleBackground)
                                }

                                data.date == startDate -> {
                                    textView.setTextColorRes(R.color.white)
                                    continuousBgView.applyBackground(rangeStartBackground)
                                    roundBgView.applyBackground(singleBackground)
                                }

                                startDate != null && endDate != null && (data.date > startDate && data.date < endDate) -> {
                                    textView.setTextColorRes(R.color.example_4_grey)
                                    continuousBgView.applyBackground(rangeMiddleBackground)
                                }

                                data.date == endDate -> {
                                    textView.setTextColorRes(R.color.white)
                                    continuousBgView.applyBackground(rangeEndBackground)
                                    roundBgView.applyBackground(singleBackground)
                                }

                                data.date == today -> {
                                    textView.setTextColorRes(R.color.example_4_grey)
                                    roundBgView.applyBackground(todayBackground)
                                }

                                else -> textView.setTextColorRes(R.color.example_4_grey)
                            }
                        }
                    }

                    DayPosition.InDate ->
                        if (startDate != null && endDate != null &&
                            isInDateBetweenSelection(data.date, startDate, endDate)
                        ) {
                            continuousBgView.applyBackground(rangeMiddleBackground)
                        }

                    DayPosition.OutDate ->
                        if (startDate != null && endDate != null &&
                            isOutDateBetweenSelection(data.date, startDate, endDate)
                        ) {
                            continuousBgView.applyBackground(rangeMiddleBackground)
                        }
                }
            }

            private fun View.applyBackground(drawable: Drawable) {
                makeVisible()
                background = drawable
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView = HeaderCalendarBinding.bind(view).exFourHeaderText
        }
        binding.exFourCalendar.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)

                @RequiresApi(Build.VERSION_CODES.O)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    container.textView.text = data.yearMonth.displayText()
                }
            }
    }
}