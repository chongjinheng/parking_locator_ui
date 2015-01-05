package com.project.jinheng.fyp.classes;

/**
 * Created by JinHeng on 1/5/2015.
 */
public class Price {

    private Integer priceID;

    private Long flatRate;

    private Long firstHour;

    private Long subsHour;

    private Long lostTicPenalty;

    private String priceType;

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public Integer getPriceID() {
        return priceID;
    }

    public void setPriceID(Integer priceID) {
        this.priceID = priceID;
    }

    public Long getFlatRate() {
        return flatRate;
    }

    public void setFlatRate(Long flatRate) {
        this.flatRate = flatRate;
    }

    public Long getFirstHour() {
        return firstHour;
    }

    public void setFirstHour(Long firstHour) {
        this.firstHour = firstHour;
    }

    public Long getSubsHour() {
        return subsHour;
    }

    public void setSubsHour(Long subsHour) {
        this.subsHour = subsHour;
    }

    public Long getLostTicPenalty() {
        return lostTicPenalty;
    }

    public void setLostTicPenalty(Long lostTicPenalty) {
        this.lostTicPenalty = lostTicPenalty;
    }
}
