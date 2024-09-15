package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.dto.Meta;
import vn.hoidanit.jobhunter.domain.dto.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.CompanyRepository;

@Service
public class CompanyService {

	private final CompanyRepository companyRepository;

	public CompanyService(CompanyRepository companyRepository) {
		this.companyRepository = companyRepository;
	}

	public Company handleCreateCompany(Company c) {
		return this.companyRepository.save(c);
	}

	public ResultPaginationDTO handleGetCompany(Pageable pageable) {
		Page<Company> pageUser = this.companyRepository.findAll(pageable);
		ResultPaginationDTO rs = new ResultPaginationDTO();
		Meta mt = new Meta();

		mt.setPage(pageUser.getNumber() + 1);
		mt.setPageSize(pageUser.getSize());

		mt.setPages(pageUser.getTotalPages());
		mt.setTotal(pageUser.getTotalElements());

		rs.setMeta(mt);
		rs.setResult(pageUser.getContent());
		return rs;
	}

	public Company handleUpdateCompany(Company c) {
		Optional<Company> companyOptional = this.companyRepository.findById(c.getId());

		if (companyOptional.isPresent()) {
			Company currentCompany = companyOptional.get();
			currentCompany.setLogo(c.getLogo());
			currentCompany.setName(c.getName());
			currentCompany.setDescription(c.getDescription());
			currentCompany.setAddress(c.getAddress());
			return this.companyRepository.save(currentCompany);
		}
		return null;
	}

	public void handleDeleteCompany(long id) {
		this.companyRepository.deleteById(id);
	}
}
