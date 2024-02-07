package com.example.locationdetect;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatePickerFragment extends BottomSheetDialogFragment {
    private EditText editTextTo;
    private EditText editTextFrom;

    Button button;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_shitactivity, container, false);

        editTextTo = view.findViewById(R.id.toDate);
        editTextFrom = view.findViewById(R.id.fromDate);
        button = view.findViewById(R.id.filter);


        // Set today's date as the default text
        setCurrentDate(editTextTo);
        setCurrentDate(editTextFrom);

        // Set onFocusChange listeners to show DatePickerDialog
        setDatePickerOnFocusChange(editTextTo);
        setDatePickerOnFocusChange(editTextFrom);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fromDate = editTextTo.getText().toString();
                String toDate = editTextFrom.getText().toString();
                if (!fromDate.isEmpty() && !toDate.isEmpty()) {
                    // Notify the MapsActivity to filter locations based on selected dates
                    if (getActivity() instanceof MapsActivity) {
                        ((MapsActivity) getActivity()).filterLocationsByDates(fromDate, toDate);
                    }
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Please select both start and end dates", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void setCurrentDate(EditText editText) {
        // Get today's date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());

        // Set today's date as the default text
        editText.setText(currentDate);
    }

    private void setDatePickerOnFocusChange(final EditText editText) {
        // Set onFocusChange listener to show DatePickerDialog and hide the keyboard
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Hide the keyboard
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }

                    // Show DatePickerDialog
                    showDatePicker(editText);
                }
            }
        });

        // Set inputType to none to prevent the keyboard from showing
        editText.setInputType(0);
    }

    private void showDatePicker(final EditText editText) {
        // Get the current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a date picker dialog with today's date as default
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                        // Handle the selected date (update the associated EditText)
                        String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDayOfMonth;
                        editText.setText(selectedDate);
                    }
                },
                year, month, dayOfMonth);
        datePickerDialog.show();
    }

}