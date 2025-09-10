
package com.event.babyshower.controller;

import com.event.babyshower.dao.RsvpRepository;
import com.event.babyshower.model.EventProperties;
import com.event.babyshower.business.MailService;
import com.event.babyshower.model.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class RsvpController {
    private final RsvpRepository repo;
    private final MailService mail;
    private final EventProperties eventProps;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("event", eventProps);
        return "index"; // HERO ONLY
    }

    @GetMapping("/rsvp")
    public String rsvp(Model model) {
        model.addAttribute("event", eventProps);
        model.addAttribute("form", new RsvpForm("", "", "", 1, ""));
        return "rsvp";
    }

    @PostMapping(value = "/rsvp", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String submitRsvp(@Valid RsvpForm form,
                             BindingResult result,
                             Model model,
                             HttpServletRequest req,
                             RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("event", eventProps);
            return "rsvp";
        }
        Rsvp r = Rsvp.builder()
                .name(form.name())
                .email(form.email())
                .phone((form.phone()==null||form.phone().isBlank())?null:form.phone())
                .guestCount(form.guestCount())
                .notes((form.notes()==null||form.notes().isBlank())?null:form.notes())
                .createdAt(Instant.now())
                .ip(getClientIp(req))
                .ua(req.getHeader("User-Agent"))
                .build();
        repo.save(r);
        try {
           // mail.sendOwnerNotification(r);
        } catch (Exception e) {
            System.out.println(e);
        }
        ra.addFlashAttribute("name", r.getName());
        return "redirect:/thanks";
    }

    @GetMapping("/thanks")
    public String thanks(Model model, @ModelAttribute("name") String name) {
        model.addAttribute("event", eventProps);
        model.addAttribute("name", name);
        return "thanks";
    }

    @GetMapping("/admin")
    public String admin(@RequestParam(value="q", required=false) String q, Model model) {
        List<Rsvp> data = StringUtils.hasText(q)
                ? repo.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByCreatedAtDesc(q, q)
                : repo.findAll().stream().sorted((a,b)->b.getCreatedAt().compareTo(a.getCreatedAt())).toList();
        model.addAttribute("rows", data);
        model.addAttribute("q", q==null?"":q);
        return "admin";
    }

    @GetMapping(value="/admin/export.csv", produces="text/csv")
    public ResponseEntity<String> exportCsv(@RequestParam(value="q", required=false) String q){
        List<Rsvp> data = StringUtils.hasText(q)
                ? repo.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByCreatedAtDesc(q, q)
                : repo.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Name,Email,Phone,Guests,Notes,Submitted,IP,UserAgent\n");
        DateTimeFormatter fmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        data.stream().sorted((a,b)->b.getCreatedAt().compareTo(a.getCreatedAt())).forEach(r -> {
            sb.append(csv(r.getName())).append(',')
                    .append(csv(r.getEmail())).append(',')
                    .append(csv(nvl(r.getPhone()))).append(',')
                    .append(r.getGuestCount()).append(',')
                    .append(csv(nvl(r.getNotes()))).append(',')
                    .append(csv(fmt.format(ZonedDateTime.ofInstant(r.getCreatedAt(), java.time.ZoneId.systemDefault())))).append(',')
                    .append(csv(nvl(r.getIp()))).append(',')
                    .append(csv(nvl(r.getUa()))).append('\n');
        });
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rsvps.csv")
                .body(sb.toString());
    }

    @GetMapping(value="/ics", produces="text/calendar")
    public ResponseEntity<String> ics(){
        String dtStart = toIcs(eventProps.getStartIso());
        String dtEnd   = toIcs(eventProps.getEndIso());
        String ics = String.join("\r\n",
                "BEGIN:VCALENDAR","VERSION:2.0","PRODID:-//BabyShower//EN",
                "BEGIN:VEVENT",
                "UID:" + java.util.UUID.randomUUID() + "@babyshower",
                "DTSTAMP:" + toIcs(java.time.Instant.now().toString()),
                "DTSTART:" + dtStart,
                "DTEND:" + dtEnd,
                "SUMMARY:" + esc(eventProps.getTitle()),
                "LOCATION:" + esc(eventProps.getVenue() + " — " + eventProps.getAddress()),
                "END:VEVENT","END:VCALENDAR");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=baby-shower.ics")
                .body(ics);
    }

    private static String getClientIp(HttpServletRequest req){
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) return ip.split(",")[0].trim();
        return req.getRemoteAddr();
    }
    private static String csv(String v){ return "\"" + v.replace("\"","\"\"") + "\""; }
    private static String nvl(String s){ return s==null?"":s; }
    private static String esc(String s){ return URLEncoder.encode(s, StandardCharsets.UTF_8); }
    private static String toIcs(String iso){
        return java.time.OffsetDateTime.parse(iso).toInstant().toString()
                .replaceAll("[-:]", "").replace(".000Z","Z");
    }
}
