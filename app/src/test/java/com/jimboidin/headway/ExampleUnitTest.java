package com.jimboidin.headway;

import com.github.mikephil.charting.data.Entry;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * jimboidin local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class jimboidinUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void datasetLogicTests(){
        final ArrayList<Entry> yValues = new ArrayList<>();
        final ArrayList<Entry> yValuesWeight = new ArrayList<>();
        final ArrayList<String> xLabels = new ArrayList<>();


        //Day 1
        yValues.add(new Entry(0, 2000f));
        yValuesWeight.add(new Entry(0, 60f));
        xLabels.add("01/11");

        //Day 2
        yValues.add(new Entry(1, 2500f));
        yValuesWeight.add(new Entry(1, 65f));
        xLabels.add("02/11");

        //Day 3

        //Day 4
        yValues.add(new Entry(2, 3000f));
        yValuesWeight.add(new Entry(2, 70f));
        xLabels.add("04/11");

        //Day 5
        yValuesWeight.add(new Entry(3, 75f));
        xLabels.add("05/11");

        for (int i = 0; i < yValues.size(); i++){
            System.out.println(yValues.get(i));
        }
        for (int i = 0; i < yValuesWeight.size(); i++){
            System.out.println(yValuesWeight.get(i));
        }
        for (int i = 0; i < xLabels.size(); i++){
            System.out.println(xLabels.get(i));
        }

    }
}