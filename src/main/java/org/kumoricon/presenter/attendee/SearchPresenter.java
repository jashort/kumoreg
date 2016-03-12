package org.kumoricon.presenter.attendee;

import org.kumoricon.model.attendee.Attendee;
import org.kumoricon.model.attendee.AttendeeRepository;
import org.kumoricon.view.attendee.AttendeeDetailView;
import org.kumoricon.view.attendee.SearchView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@Scope("request")
public class SearchPresenter {
    @Autowired
    private AttendeeRepository attendeeRepository;

    private SearchView view;

    public SearchPresenter() {
    }

    public void searchChanged(String searchString) {
        if (searchString != null) {
            view.navigateTo(view.VIEW_NAME + "/" + searchString.trim());
        }
    }

    public void searchFor(String searchString) {
        view.getAttendeeBeanList().removeAllItems();
        if (searchString != null && !searchString.trim().isEmpty()) {
            searchString = searchString.trim();
            List<Attendee> attendees = attendeeRepository.findByLastNameOrBadgeNumber(searchString);
            if (attendees.size() > 0) {
                view.getAttendeeBeanList().addAll(attendees);
            } else {
                view.notify("No matching attendees found");
            }
        }
    }

    public void selectAttendee(Attendee attendee) {
        view.navigateTo(AttendeeDetailView.VIEW_NAME + "/" + attendee.getId().toString());
    }

    public SearchView getView() { return view; }
    public void setView(SearchView view) { this.view = view; }
}