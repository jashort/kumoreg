package org.kumoricon.site.report.till;

import com.vaadin.navigator.View;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.Label;
import org.kumoricon.site.BaseView;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@SpringView(name = org.kumoricon.site.report.till.TillReportView.VIEW_NAME)
public class TillReportView extends BaseView implements View {
    public static final String VIEW_NAME = "tillReport";
    public static final String REQUIRED_RIGHT = "view_till_report";

    private final Button btnRefresh = new Button("Refresh");
    private final Label data = new Label();

    private final TillReportPresenter handler;

    @Autowired
    public TillReportView(TillReportPresenter handler) {
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        addComponents(data, btnRefresh);
        data.setContentMode(ContentMode.HTML);
        data.setWidth("1000px");
        data.setHeightUndefined();

        btnRefresh.addClickListener((Button.ClickListener) clickEvent -> {
            btnRefresh.setCaption("Refresh");
            handler.showAllTills(this);
        });

        handler.showAllTills(this);
    }

    public void showData(String report) { data.setValue(report); }

    public String getRequiredRight() { return REQUIRED_RIGHT; }
}