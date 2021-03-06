/**
 * DocEditPlaceActivity.java
 * Dec 14, 2014
 * Sarang Joshi
 */

package com.sarangjoshi.docschedulerdoc;

import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class DocEditPlaceActivity extends FragmentActivity {
    // private List<DayTime> dayTimes;
    private Place oldPlace;
    private Place place;

    private Spinner day;
    private Button startTime, endTime;
    private ListView dayTimesList;
    private EditText placeName;
    private DayTimeAdapter dayTimesAdapter;

    private int sH, sM, eH, eM;
    private boolean sChanged = false, eChanged = false;

    private int code;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_editplace);

        // Sets up views
        day = (Spinner) findViewById(R.id.daySelector);
        dayTimesList = (ListView) findViewById(R.id.dayTimes);
        startTime = (Button) findViewById(R.id.startTime);
        endTime = (Button) findViewById(R.id.endTime);
        placeName = (EditText) findViewById(R.id.placeNameText);

        startTime.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setTime(true);
            }
        });
        endTime.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setTime(false);
            }
        });

        // Checks for entry mode
        Intent intent = getIntent();
        if (intent.getIntExtra(DocEditScheduleActivity.PLACE_INDEX, -1) >= 0) {
            // edit mode
            code = DocEditScheduleActivity.EDIT_CODE;
            place = DocHomeActivity.getSchedule().getPlace(
                    intent.getIntExtra(DocEditScheduleActivity.PLACE_INDEX, -1));
            placeName.setText(place.getName());
        } else {
            // add mode
            code = DocEditScheduleActivity.ADD_CODE;
            place = new Place();
        }

        // Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.days, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        day.setAdapter(adapter);

        // ListView
        // dayTimes = new ArrayList<DayTime>();
        dayTimesAdapter = new DayTimeAdapter(this, place.getDayTimes());
        dayTimesList.setAdapter(dayTimesAdapter);
        dayTimesList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                delete(position);
            }

        });
    }

    /**
     * Deletes the DayTime object at the given position.
     */
    private void delete(final int position) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        place.getDayTimes().remove(position);
                        dayTimesAdapter.notifyDataSetChanged();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    /**
     * Opens a dialog to set the time.
     *
     * @param isStart whether this is the start time picker or not
     */
    public void setTime(final boolean isStart) {
        final int h, m;

        final Calendar c = Calendar.getInstance();
        if (sChanged && eChanged) {
            h = (isStart) ? sH : eH;
            m = (isStart) ? sM : eM;
        } else if (!sChanged && !eChanged) {
            h = c.get(Calendar.HOUR_OF_DAY);
            m = c.get(Calendar.MINUTE);
        } else if (sChanged && !eChanged) {
            h = sH;
            m = sM;
        } else {
            h = eH;
            m = eM;
        }

        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.setTimeSetListener(
                new OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int h, int m) {
                        if (isStart) {
                            sH = h;
                            sM = m;
                            sChanged = true;
                        } else {
                            eH = h;
                            eM = m;
                            eChanged = true;
                        }
                        (isStart ? startTime : endTime).setText(DayTime
                                .getTimeAsString(h, m));
                    }
                });
        newFragment.setHr(h);
        newFragment.setMin(m);
        newFragment.show(getSupportFragmentManager(), (isStart) ? "stimepicker"
                : "etimepicker");
    }

    /**
     * Saves the given time slot for this place.
     */
    public void saveTime(View v) {
        if (sChanged && eChanged) {
            try {
                if (isValid()) {
                    place.addDayTime(new DayTime(day.getSelectedItem()
                            .toString(), sH, sM, eH, eM));
                    dayTimesAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Time isn't valid.",
                            Toast.LENGTH_SHORT);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error, try again.", Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            Toast.makeText(this, "Please check start and end times.",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Checks if the current time is valid.
     */
    private boolean isValid() {
        if (getMin(sH, sM) >= getMin(eH, eM)) {
            return false;
        }
        return true;
    }

    private int getMin(int h, int m) {
        return 24 * h + m;
    }

    /**
     * Private ArrayAdapter for the times.S
     *
     * @author Sarang
     */
    private class DayTimeAdapter extends ArrayAdapter<DayTime> {
        private final Context mContext;

        public DayTimeAdapter(Context context, List<DayTime> objects) {
            super(context, R.layout.layout_daytime, objects);

            mContext = context;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View v = inflater.inflate(R.layout.layout_daytime, parent, false);
            TextView day, start, end;

            day = (TextView) v.findViewById(R.id.dayTimeDay);
            start = (TextView) v.findViewById(R.id.dayTimeStart);
            end = (TextView) v.findViewById(R.id.dayTimeEnd);

            DayTime t = place.getDayTimes().get(pos);

            day.setText(t.getDay());
            start.setText(t.getStartString());
            end.setText(t.getEndString());

            return v;
        }
    }

    /**
     * Saves the place created/edited in this Activity.
     */
    public void savePlace(View v) {
        if (placeName.getText().length() == 0) {
            Toast.makeText(this, "Please enter a place name.",
                    Toast.LENGTH_SHORT).show();
        } else if (place.nOfDayTimes() == 0) {
            Toast.makeText(this, "Please add times.", Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(this, "Place saved!", Toast.LENGTH_SHORT).show();
            Intent intent = getIntent();

            place.setName(placeName.getText().toString());

            if (code == DocEditScheduleActivity.ADD_CODE) {
                int index = DocHomeActivity.getSchedule().addPlace(place);
                intent.putExtra(DocEditScheduleActivity.PLACE_INDEX, index);
            }

            setResult(RESULT_OK, intent);
            finish();
        }
    }

    public void onBackPressed() {
        savePlace(null);
    }
}
