package coffe.baseproject.data.dto.profile;

import jakarta.validation.constraints.Size;

public class ProfileUpdateRequestDTO {

    @Size(max = 255)
    private String fullName;

    @Size(max = 255)
    private String phone;

    @Size(max = 255)
    private String cpf;

    @Size(max = 255)
    private String cnpj;

    @Size(max = 255)
    private String address;

    @Size(max = 255)
    private String zipCode;

    private Boolean company;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Boolean getCompany() {
        return company;
    }

    public void setCompany(Boolean company) {
        this.company = company;
    }
}
