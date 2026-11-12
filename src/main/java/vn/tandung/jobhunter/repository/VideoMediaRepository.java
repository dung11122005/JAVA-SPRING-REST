package vn.tandung.jobhunter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.tandung.jobhunter.domain.VideoMedia;

@Repository 
public interface VideoMediaRepository extends JpaRepository<VideoMedia, Long> {
    // Tìm video đang hoạt động của một thực thể (Company/User/Job) cụ thể
    Optional<VideoMedia> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
}
