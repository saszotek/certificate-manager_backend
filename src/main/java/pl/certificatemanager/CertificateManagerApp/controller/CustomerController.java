package pl.certificatemanager.CertificateManagerApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.certificatemanager.CertificateManagerApp.model.Customer;
import pl.certificatemanager.CertificateManagerApp.service.CustomerService;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping("/find/all")
    public ResponseEntity<Map<String, Object>> getCustomers(@RequestParam(required = false) String email,
                                                            @RequestParam(defaultValue = "asc") String order,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "3") int size) {
        List<Customer> customers;
        Pageable pageable = PageRequest.of(page, size);

        Page<Customer> customerPage;

        if (email.equals("") && order.equals("asc")) {
            customerPage = customerService.getCustomersByValidToAsc(pageable);
        } else if (email.equals("") && order.equals("desc")) {
            customerPage = customerService.getCustomersByValidToDesc(pageable);
        } else {
            customerPage = customerService.getCustomersByEmail(email, pageable);
        }

        customers = customerPage.getContent();

        Map<String, Object> response = new HashMap<>();
        response.put("customers", customers);
        response.put("currentPage", customerPage.getNumber());
        response.put("totalItems", customerPage.getTotalElements());
        response.put("totalPages", customerPage.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/find/id/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(customerService.getCustomerById(id));
    }

    @PostMapping("/save")
    public ResponseEntity<Customer> saveCustomer(@RequestBody Customer customer) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/customer/save").toUriString());
        return ResponseEntity.created(uri).body(customerService.saveCustomer(customer));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Customer> deleteCustomer(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(customerService.deleteCustomer(id));
    }

    @PostMapping("/save/{customerId}/invoice/{invoiceId}")
    public ResponseEntity<Customer> saveInvoiceToCustomer(@PathVariable("customerId") Long customerId, @PathVariable("invoiceId") Long invoiceId) {
        Customer customer = customerService.saveInvoiceToCustomer(customerId, invoiceId);
        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{customerId}/invoice/{invoiceId}")
    public ResponseEntity<Customer> deleteInvoiceFromCustomer(@PathVariable("customerId") Long customerId, @PathVariable("invoiceId") Long invoiceId) {
        Customer customer = customerService.deleteInvoiceFromCustomer(customerId, invoiceId);
        return new ResponseEntity<>(customer, HttpStatus.OK);
    }
}
