package pl.certificatemanager.CertificateManagerApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.certificatemanager.CertificateManagerApp.exception.InvoiceAlreadyAssignedCustomerException;
import pl.certificatemanager.CertificateManagerApp.model.Customer;
import pl.certificatemanager.CertificateManagerApp.model.Invoice;
import pl.certificatemanager.CertificateManagerApp.repository.CustomerRepo;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomerService {
    private final CustomerRepo customerRepo;
    private final InvoiceService invoiceService;

    public Page<Customer> getCustomers(Pageable pageable) {
        log.info("Fetching all customers");
        return customerRepo.findAll(pageable);
    }

    public Page<Customer> getCustomersByLastName(String lastName, Pageable pageable) {
        return customerRepo.findByLastNameContaining(lastName, pageable);
    }

    public Customer getCustomerById(Long id) {
        log.info("Fetching customer with id {}", id);
        return customerRepo.findCustomerById(id);
    }

    public Customer saveCustomer(Customer customer) {
        log.info("Saving a new customer {} to the database", customer.getLastName());
        return customerRepo.save(customer);
    }

    public Customer deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        log.info("Deleting customer {}", customer);
        customerRepo.delete(customer);
        return customer;
    }

    public Customer saveInvoiceToCustomer(Long customerId, Long invoiceId) {
        Customer customer = getCustomerById(customerId);
        Invoice invoice = invoiceService.getInvoiceById(invoiceId);
        if (Objects.nonNull(invoice.getCustomer())) {
            throw new InvoiceAlreadyAssignedCustomerException(invoiceId, invoice.getCustomer().getId());
        }
        log.info("Invoice {} was added to the customer {}", invoiceId, customerId);
        customer.addInvoice(invoice);
        invoice.setCustomer(customer);
        return customer;
    }

    public Customer deleteInvoiceFromCustomer(Long customerId, Long invoiceId) {
        Customer customer = getCustomerById(customerId);
        Invoice invoice = invoiceService.getInvoiceById(invoiceId);
        log.info("Invoice {} is now deleted from the customer: {}", invoiceId, customerId);
        customer.deleteInvoice(invoice);
        return customer;
    }
}
