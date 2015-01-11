package com.project.jinheng.fyp.classes;

import java.util.List;

/**
 * Created by JinHeng on 1/11/2015.
 */
public class GoogleNearbyPlaces {

    private List<SearchClasses.Result> results;
    private String status;

    public List<SearchClasses.Result> getResults() {
        return results;
    }

    public void setResults(List<SearchClasses.Result> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
