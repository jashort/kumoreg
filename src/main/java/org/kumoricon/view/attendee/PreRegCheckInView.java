package org.kumoricon.view.attendee;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import org.kumoricon.model.attendee.Attendee;
import org.kumoricon.model.badge.Badge;
import org.kumoricon.presenter.attendee.PreRegCheckInPresenter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

@ViewScope
@SpringView(name = PreRegCheckInView.VIEW_NAME)
public class PreRegCheckInView extends VerticalLayout implements View {
    public static final String VIEW_NAME = "preregcheckin";

    @Autowired
    private PreRegCheckInPresenter handler;
    private AttendeeDetailForm form;
    private CheckBox informationVerified = new CheckBox("Information Verified");
    private CheckBox consentFormReceived = new CheckBox("Parental Consent Form Received");
    private Button btnCheckIn = new Button("Check In");
    private Button btnCancel = new Button("Cancel");

    @PostConstruct
    public void init() {
        handler.setView(this);
        setMargin(true);
        setSpacing(true);
        form = new AttendeePreRegDetailForm();
        form.setAllFieldsButCheckInEnabled();
        addComponent(form);
        addComponent(buildVerifiedCheckboxes());
        addComponent(buildSaveCancel());
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        String parameters = viewChangeEvent.getParameters();
        if (parameters != null && !parameters.equals("")) {
            handler.showAttendee((Integer.parseInt(viewChangeEvent.getParameters())));
        }
    }

    private HorizontalLayout buildVerifiedCheckboxes() {
        HorizontalLayout h = new HorizontalLayout();
        h.setMargin(false);
        h.setSpacing(true);

        h.addComponent(informationVerified);
        h.addComponent(consentFormReceived);

        return h;
    }

    private HorizontalLayout buildSaveCancel() {
        HorizontalLayout h = new HorizontalLayout();
        h.setMargin(false);
        h.setSpacing(true);

        btnCheckIn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                handler.checkInAttendee();
            }
        });
        btnCancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                handler.cancelAttendee();
            }
        });
        h.addComponent(btnCheckIn);
        h.addComponent(btnCancel);
        return h;
    }

    public PreRegCheckInPresenter getHandler() { return handler; }
    public void setHandler(PreRegCheckInPresenter presenter) { this.handler = presenter; }

    public AttendeeDetailForm getDetailForm() { return form; }


    public Boolean informationVerified() { return informationVerified.getValue(); }
    public Boolean parentalConsentFormReceived() { return consentFormReceived.getValue(); }

    public Attendee getAttendee() {return form.getAttendee(); }

    public void showAttendee(Attendee attendee) {
        form.show(attendee);

        informationVerified.setValue(false);
        consentFormReceived.setValue(attendee.getParentFormReceived());
        if (attendee.isMinor() && !attendee.getCheckedIn()) {
            consentFormReceived.setEnabled(true);
        } else {
            consentFormReceived.setEnabled(false);
        }
        if (attendee.getCheckedIn()) {
            btnCheckIn.setEnabled(false);
            informationVerified.setEnabled(false);
            btnCheckIn.setCaption("Already Checked In");
        } else {
            btnCheckIn.setEnabled(true);
            informationVerified.setEnabled(true);
            btnCheckIn.setCaption("Check In");
        }
    }

    public void setAvailableBadges(List<Badge> availableBadges) {
        form.setAvailableBadges(availableBadges);
    }

}
