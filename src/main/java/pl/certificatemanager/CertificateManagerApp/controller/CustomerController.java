package pl.certificatemanager.CertificateManagerApp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.certificatemanager.CertificateManagerApp.model.Customer;
import pl.certificatemanager.CertificateManagerApp.service.CustomerService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping("/find/all")
    public ResponseEntity<List<Customer>> getCustomers() {
        return ResponseEntity.ok().body(customerService.getCustomers());
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
