package coffe.baseproject.data.dto.profile;

import coffe.baseproject.model.Profile;

public class ProfileResponseDTO {

    private final Long id;
    private final String fullName;
    private final String phone;
    private final String cpf;
    private final String cnpj;
    private final String address;
    private final String zipCode;
    private final Boolean company;

    public ProfileResponseDTO(Profile profile) {
        this(profile.getId(), profile.getFullName(), profile.getPhone(), profile.getCpf(), profile.getCnpj(),
                profile.getAddress(), profile.getZipCode(), profile.getCompany());
    }

    public ProfileResponseDTO(Long id, String fullName, String phone, String cpf, String cnpj,
                              String address, String zipCode, Boolean company) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.cpf = cpf;
        this.cnpj = cnpj;
        this.address = address;
        this.zipCode = zipCode;
        this.company = company;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getCpf() {
        return cpf;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getAddress() {
        return address;
    }

    public String getZipCode() {
        return zipCode;
    }

    public Boolean getCompany() {
        return company;
    }
}
