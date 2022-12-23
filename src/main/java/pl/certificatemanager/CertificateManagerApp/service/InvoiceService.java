package pl.certificatemanager.CertificateManagerApp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.certificatemanager.CertificateManagerApp.exception.CertificateAlreadyAssignedInvoiceException;
import pl.certificatemanager.CertificateManagerApp.model.Certificate;
import pl.certificatemanager.CertificateManagerApp.model.Invoice;
import pl.certificatemanager.CertificateManagerApp.repository.InvoiceRepo;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InvoiceService {
    private final InvoiceRepo invoiceRepo;
    private final CertificateService certificateService;

    public List<Invoice> getInvoices() {
        log.info("Fetching all invoices");
        return invoiceRepo.findAll();
    }

    public Invoice getInvoiceById(Long id) {
        log.info("Fetching invoice with id {}", id);
        return invoiceRepo.findInvoiceById(id);
    }

    public Invoice saveInvoice(Invoice invoice) {
        log.info("Saving a new invoice {} to the database", invoice.getInvoiceNumber());
        return invoiceRepo.save(invoice);
    }

    public Invoice deleteInvoice(Long id) {
        Invoice invoice = getInvoiceById(id);
        log.info("Deleting invoice {}", invoice);
        invoiceRepo.delete(invoice);
        return invoice;
    }

    public Invoice saveCertificateToInvoice(Long invoiceId, Long certificateId) {
        Invoice invoice = getInvoiceById(invoiceId);
        Certificate certificate = certificateService.getCertificateById(certificateId);
        if (Objects.nonNull(certificate.getInvoice())) {
            throw new CertificateAlreadyAssignedInvoiceException(certificateId, certificate.getInvoice().getId());
        }
        log.info("Certificate {} was added to the invoice {}", certificateId, invoiceId);
        invoice.addCertificate(certificate);
        certificate.setInvoice(invoice);
        return invoice;
    }

    public Invoice deleteCertificateFromInvoice(Long invoiceId, Long certificateId) {
        Invoice invoice = getInvoiceById(invoiceId);
        Certificate certificate = certificateService.getCertificateById(certificateId);
        log.info("Certificate {} is now deleted from the invoice: {}", certificateId, invoiceId);
        invoice.deleteCertificate(certificate);
        return invoice;
    }
}
