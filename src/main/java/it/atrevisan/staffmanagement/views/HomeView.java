package it.atrevisan.staffmanagement.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexWrap;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import it.atrevisan.staffmanagement.dto.AbsenceDTO;
import it.atrevisan.staffmanagement.dto.ContractDTO;
import it.atrevisan.staffmanagement.dto.PersonDTO;
import it.atrevisan.staffmanagement.dto.UserDTO;
import it.atrevisan.staffmanagement.enums.Roles;
import it.atrevisan.staffmanagement.service.AbsenceService;
import it.atrevisan.staffmanagement.service.ContractService;
import it.atrevisan.staffmanagement.service.PersonService;
import it.atrevisan.staffmanagement.views.config.MainLayout;
import it.atrevisan.staffmanagement.views.config.Routes;
import it.atrevisan.staffmanagement.views.session.BasicLoggedInView;
import it.atrevisan.staffmanagement.views.session.SessionUtils;
import it.atrevisan.staffmanagement.views.utils.NotificationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Route(value = Routes.HOME, layout = MainLayout.class)
public class HomeView extends BasicLoggedInView {

    private final PersonService personService;
    private final ContractService contractService;
    private final AbsenceService absenceService;

    private final DatePicker startDatePicker = new DatePicker("From");
    private final DatePicker endDatePicker = new DatePicker("To");

    public HomeView(PersonService personService,
                        ContractService contractService,
                        AbsenceService absenceService) {
        this.personService = personService;
        this.contractService = contractService;
        this.absenceService = absenceService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        buildDashboard();
    }

    private void buildDashboard() {
        removeAll();

        Optional<UserDTO> currentUserOpt = Optional.ofNullable(SessionUtils.getSessionUser());
        if (!currentUserOpt.isPresent()) {
            add(new Span("No user logged in"));
            return;
        }

        UserDTO currentUser = currentUserOpt.get();

        H2 welcome = new H2("Welcome, " + currentUser.getUsername());
        add(welcome);

        // Griglia card: 2 righe max, wrap automatico
        FlexLayout grid = new FlexLayout();
        grid.setSizeFull();
        grid.setFlexWrap(FlexWrap.WRAP);
        grid.getStyle().set("gap", "20px");

        // Card ruoli
        grid.add(buildRolesCard(currentUser));

        // Card persona
        VerticalLayout personCard = buildPersonCard(currentUser);
        if(personCard != null) grid.add(personCard);

        // Card contratti
        VerticalLayout contractsCard = buildContractsCard(currentUser);
        if(contractsCard != null) grid.add(contractsCard);

        // Card assenze
        VerticalLayout absencesCard = buildAbsencesCard(currentUser);
        if(absencesCard != null) grid.add(absencesCard);

        add(grid);
    }

    private VerticalLayout buildRolesCard(UserDTO user) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("300px");
        styleCard(card, "#e8f0fe"); // azzurro chiaro

        H2 title = new H2("Roles");
        title.getStyle().set("margin", "0");
        Icon icon = VaadinIcon.USERS.create();
        title.add(icon);

        card.add(title);

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            user.getRoles().forEach(role -> {
                Span r = new Span(role);
                r.getStyle().set("font-weight", "bold");
                card.add(r);
            });
        } else {
            card.add(new Span("No roles assigned"));
        }

        return card;
    }

    private VerticalLayout buildPersonCard(UserDTO user) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("300px");
        styleCard(card, "#d1ffd6"); // verde chiaro

        H2 title = new H2("Person Info");
        title.getStyle().set("margin", "0");
        Icon icon = VaadinIcon.USER.create();
        title.add(icon);

        card.add(title);

        if (user.getPersonDocumentId() != null) {
            Optional<PersonDTO> personOpt = personService.getPerson(user.getPersonDocumentId());
            if (personOpt.isPresent()) {
                PersonDTO p = personOpt.get();
                card.add(new Span("Name: " + p.getName() + " " + p.getSurname()));
                card.add(new Span("Email: " + p.getEmail()));
                card.add(new Span("Phone: " + p.getPhone()));
                card.add(new Span("Address: " + p.getAddress()));
            } else {
                card.add(new Span("Person info not found"));
            }
        } else {
            return null;
        }

        return card;
    }

    private VerticalLayout buildContractsCard(UserDTO user) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("300px");
        styleCard(card, "#fff4e5"); // arancio chiaro

        H2 title = new H2("Active Contracts");
        title.getStyle().set("margin", "0");
        Icon icon = VaadinIcon.BOOK.create();
        title.add(icon);
        card.add(title);

        if (user.getPersonDocumentId() != null) {
            List<ContractDTO> contracts = contractService.getAllContracts()
                    .stream()
                    .filter(c -> c.getPersonDocumentId().equals(user.getPersonDocumentId())
                            && (c.getEndDate() == null || !c.getEndDate().isBefore(LocalDate.now())))
                    .collect(Collectors.toList());

            if (contracts.isEmpty()) {
                card.add(new Span("No active contracts"));
            } else {
                contracts.forEach(c -> card.add(new Span(c.getJobRole() + " (" + c.getStartDate() + " - " +
                        (c.getEndDate() != null ? c.getEndDate() : "Ongoing") + ")")));
            }
        } else {
            return null;
        }

        return card;
    }

    private VerticalLayout buildAbsencesCard(UserDTO user) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("600px");          // larghezza maggiore
        card.setHeight("450px");         // altezza fissa
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle().set("border", "1px solid #f28b82");
        card.getStyle().set("border-radius", "8px");
        card.getStyle().set("background-color", "#ffe6e6");
        if(user.getPersonDocumentId() != null){
            H2 title = new H2("Absences");
            title.getStyle().set("margin", "0");
            card.add(title);

            // Filtro date
            HorizontalLayout filterLayout = new HorizontalLayout();
            filterLayout.setWidthFull();
            filterLayout.setAlignItems(FlexComponent.Alignment.BASELINE);

            DatePicker startDatePicker = new DatePicker("Start Date");
            DatePicker endDatePicker = new DatePicker("End Date");
            startDatePicker.setWidth("200px");
            endDatePicker.setWidth("200px");

            Button filterBtn = new Button("Filter");
            filterLayout.add(startDatePicker, endDatePicker, filterBtn);
            card.add(filterLayout);

            // Contenitore interno scrollabile
            VerticalLayout contentLayout = new VerticalLayout();
            contentLayout.setWidthFull();
            contentLayout.setHeight("350px");
            contentLayout.setSpacing(true);
            contentLayout.setPadding(true);
            contentLayout.getStyle().set("overflow", "auto"); // scroll se troppe assenze
            card.add(contentLayout);

            // inizializza date e contenuto
            startDatePicker.setValue(LocalDate.now().minusMonths(1));
            endDatePicker.setValue(LocalDate.now());
            updateAbsences(contentLayout, user, startDatePicker.getValue(), endDatePicker.getValue());

            // bottone filter
            filterBtn.addClickListener(e ->
                    updateAbsences(contentLayout, user, startDatePicker.getValue(), endDatePicker.getValue())
            );
        } else {
            return null;
        }

        return card;
    }

    private void updateAbsences(VerticalLayout contentLayout, UserDTO user, LocalDate start, LocalDate end) {
        contentLayout.removeAll();

        if (user.getPersonDocumentId() != null) {
            if (start == null || end == null || start.isAfter(end)) {
                NotificationService.showWarning("Invalid date range");
                return;
            }

            List<AbsenceDTO> absences = absenceService.getAllAbsences()
                    .stream()
                    .filter(a -> a.getPersonDocumentId().equals(user.getPersonDocumentId())
                            && !a.getDate().isBefore(start) && !a.getDate().isAfter(end))
                    .collect(Collectors.toList());

            if (absences.isEmpty()) {
                contentLayout.add(new Span("No absences in selected period"));
            } else {
                absences.forEach(a -> {
                    Span s = new Span(a.getDate() + ": " + a.getReason());
                    s.setWidthFull();                     // occupa tutta la card
                    s.getStyle().set("display", "block");
                    s.getStyle().set("padding", "4px 8px");
                    s.getStyle().set("background-color", "#ffcccc");
                    s.getStyle().set("border-radius", "4px");
                    contentLayout.add(s);
                });
            }
        }
    }

    private void styleCard(VerticalLayout card, String bgColor) {
        card.getStyle().set("border", "1px solid #ccc");
        card.getStyle().set("border-radius", "8px");
        card.getStyle().set("padding", "15px");
        card.getStyle().set("background-color", bgColor);
        card.getStyle().set("box-shadow", "2px 2px 6px rgba(0,0,0,0.1)");
    }

    @Override
    protected Roles[] getAllowedRoles() {
        return Roles.values();
    }
}