package com.vrd.vehicle.dto;

import java.util.List;

public class FaultAnalysisDistributionVO {
    private List<CountItem> byLevel;
    private List<CountItem> byDomain;
    private List<CountItem> byScene;
    private List<CountItem> topFaultCodes;

    public List<CountItem> getByLevel() { return this.byLevel; }
    public List<CountItem> getByDomain() { return this.byDomain; }
    public List<CountItem> getByScene() { return this.byScene; }
    public List<CountItem> getTopFaultCodes() { return this.topFaultCodes; }

    public void setByLevel(List<CountItem> byLevel) { this.byLevel = byLevel; }
    public void setByDomain(List<CountItem> byDomain) { this.byDomain = byDomain; }
    public void setByScene(List<CountItem> byScene) { this.byScene = byScene; }
    public void setTopFaultCodes(List<CountItem> topFaultCodes) { this.topFaultCodes = topFaultCodes; }

    public static class CountItem {
        private String name;
        private Long count;

        public String getName() { return this.name; }
        public Long getCount() { return this.count; }

        public void setName(String name) { this.name = name; }
        public void setCount(Long count) { this.count = count; }
    }
}
