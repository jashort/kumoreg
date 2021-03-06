package org.kumoricon.site.attendee;

import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.kumoricon.model.attendee.Attendee;
import org.kumoricon.model.attendee.AttendeeHistory;
import org.kumoricon.model.badge.Badge;
import org.kumoricon.model.user.User;
import org.kumoricon.site.BaseView;
import org.kumoricon.site.attendee.form.AttendeeDetailForm;
import org.kumoricon.site.attendee.search.AttendeeSearchPresenter;
import org.kumoricon.site.attendee.window.OverrideRequiredForEditWindow;
import org.kumoricon.site.attendee.window.OverrideRequiredWindow;
import org.kumoricon.site.attendee.window.ViewNoteWindow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.List;

@ViewScope
public abstract class AttendeeDetailView extends BaseView implements View, AttendeePrintView, DetailFormHandler {
    @Value("${kumoreg.validation.attendee.requirePhoneAndEmail:true}")
    protected Boolean requirePhoneAndEmail;

    protected Integer attendeeId;

    protected AttendeeDetailForm form = new AttendeeDetailForm();
    protected Button btnSave;
    protected Button btnCancel;
    protected Button btnCheckIn;
    protected Button btnSaveAndReprint;
    protected Button btnPrePrintBadge;
    protected Button btnAddNote;
    protected Button btnEdit;

    @Autowired
    protected AttendeeSearchPresenter handler;


    @PostConstruct
    public void init() {
//        setSizeFull();

        form.setOpitionallyRequiredFieldNames(requirePhoneAndEmail);

        VerticalLayout buttons = buildSaveCancel();
        btnSave.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        btnSave.addStyleName(ValoTheme.BUTTON_PRIMARY);

        setButtonVisibility();
        addComponents(form, buttons);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        checkPermissions();
        setButtonVisibility();
    }

    protected void setButtonVisibility() {
        btnSave.setVisible(currentUserHasRight("attendee_edit"));
        btnEdit.setVisible(currentUserHasRight("attendee_edit_with_override") && !currentUserHasRight("attendee_edit"));
        btnCheckIn.setVisible(currentUserHasRight("pre_reg_check_in"));
        btnPrePrintBadge.setVisible(currentUserHasRight("pre_print_badges"));
        btnSaveAndReprint.setVisible(currentUserHasRight("reprint_badge") || currentUserHasRight("reprint_badge_with_override"));
        if (currentUserHasRight("reprint_badge")) {
            btnSaveAndReprint.setCaption("Reprint Badge");
        }
        btnAddNote.setVisible(currentUserHasRight("attendee_add_note"));
    }


    @Override
    public void refresh() {
        handler.showAttendee(this, attendeeId);
    }

    public void showAttendee(Attendee attendee, List<Badge> all) {
        form.setAvailableBadges(all);
        form.show(attendee);

        btnCheckIn.setEnabled(!attendee.getCheckedIn());
        btnSaveAndReprint.setEnabled(attendee.getCheckedIn());
        btnPrePrintBadge.setEnabled(!attendee.getCheckedIn());

        if (currentUserHasRight("attendee_edit") ||
                currentUserHasRight("pre_reg_check_in_edit") && !attendee.getCheckedIn()) {
            form.setEditableFields(AttendeeDetailForm.EditableFields.ALL);
            form.setManualPriceEnabled(currentUserHasRight("attendee_override_price"));
        } else {
            form.setEditableFields(AttendeeDetailForm.EditableFields.NONE);
        }

    }

    public void showOverrideEditWindow(AttendeeSearchPresenter presenter, AttendeeDetailView attendeeDetailWindow) {
        OverrideRequiredForEditWindow window = new OverrideRequiredForEditWindow(presenter, "attendee_edit", attendeeDetailWindow);
        showWindow(window);
    }

    @Override
    public void showAttendeeHistory(AttendeeHistory attendeeHistory) {
        ViewNoteWindow window = new ViewNoteWindow(attendeeHistory);
        showWindow(window);
    }


    protected VerticalLayout buildSaveCancel() {
        VerticalLayout buttons = new VerticalLayout();
        buttons.setSpacing(true);
        buttons.setWidth("15%");
        buttons.setMargin(new MarginInfo(false, true, false, true));
        btnSave = new Button("Save");
        btnCancel = new Button("Cancel");
        btnEdit = new Button("Edit (Override)");
        btnEdit.addClickListener((Button.ClickListener) clickEvent -> showOverrideEditWindow(handler, this));

        btnCheckIn = new Button("Check In");
        btnCheckIn.addClickListener((Button.ClickListener) clickEvent -> showCheckInWindow());
        btnCheckIn.setVisible(currentUserHasRight("pre_reg_check_in"));

        btnAddNote = new Button("Add Note");
        btnAddNote.addClickListener((Button.ClickListener) clickEvent -> showAddNoteWindow());

        if (currentUserHasRight("reprint_badge")) {
            btnSaveAndReprint = new Button("Reprint Badge");
        } else {
            btnSaveAndReprint = new Button("Reprint Badge (Override)");
        }

        btnSave.addClickListener((Button.ClickListener) clickEvent -> {
            try {
//                form.commit();
                handler.saveAttendee(this, form.getAttendee());
                close();
            } catch (Exception e) {
                notifyError(e.getMessage());
            }
        });
        btnCancel.addClickListener((Button.ClickListener) clickEvent -> close());
        btnSaveAndReprint.addClickListener((Button.ClickListener) clickEvent -> reprintClicked());


        btnPrePrintBadge = new Button("Pre-Print Badge");
        btnPrePrintBadge.addClickListener((Button.ClickListener) clickEvent -> {
            try {
                form.commit();
                handler.saveAttendeeAndPrePrintBadge(this, form.getAttendee());
            } catch (Exception e) {
                notifyError(e.getMessage());
            }
        });

        buttons.addComponent(btnSave);
        buttons.addComponent(btnEdit);
        buttons.addComponent(btnSaveAndReprint);
        buttons.addComponent(btnAddNote);
        buttons.addComponent(btnCheckIn);
        buttons.addComponent(btnPrePrintBadge);
        buttons.addComponent(btnCancel);
        return buttons;
    }

    protected abstract void reprintClicked();


    protected void showCheckInWindow() {
        throw new RuntimeException("This function must be overridden by another view");
    }

    protected void showAddNoteWindow() {
        throw new RuntimeException("This function must be overridden by another view");
    }

    public void enableEditFields(User overrideUser) {
        if (currentUserHasRight("attendee_edit_with_override") && overrideUser.hasRight("attendee_edit")) {
            form.setEditableFields(AttendeeDetailForm.EditableFields.ALL);
            form.setManualPriceEnabled(overrideUser.hasRight("attendee_override_price"));
            btnSave.setVisible(true);
            btnSave.setEnabled(true);
            btnSave.setCaption("Save");
            if (form.getAttendee().getCheckedIn() && currentUserHasRight("reprint_badge_with_override") && overrideUser.hasRight("reprint_badge")) {
                btnSaveAndReprint.setCaption("Reprint Badge");
                btnSaveAndReprint.setEnabled(true);
                btnSaveAndReprint.setVisible(true);
            }
            btnEdit.setEnabled(false);
        }
    }
}
