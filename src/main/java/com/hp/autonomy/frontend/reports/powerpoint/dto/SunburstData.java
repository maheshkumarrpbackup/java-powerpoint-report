/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.reports.powerpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SunburstData implements ComposableElement {
    private String[] categories;
    private double[] values;
    private String title;

    public boolean validateInput() {
        return categories != null && values != null && categories.length == values.length;
    }
}