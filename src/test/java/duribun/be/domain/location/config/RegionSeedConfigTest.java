package duribun.be.domain.location.config;

import duribun.be.domain.location.entity.Region;
import duribun.be.domain.location.repository.RegionRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegionSeedConfigTest {

    private final RegionRepository regionRepository = mock(RegionRepository.class);
    private final RegionSeedConfig regionSeedConfig = new RegionSeedConfig();

    @Test
    void 저장된_지역이_없으면_초기_지역_데이터를_저장한다() throws Exception {
        when(regionRepository.count()).thenReturn(0L);

        regionSeedConfig.regionSeedRunner(regionRepository).run();

        verify(regionRepository, org.mockito.Mockito.atLeastOnce()).save(any(Region.class));
    }

    @Test
    void 이미_지역_데이터가_있으면_시드하지_않는다() throws Exception {
        when(regionRepository.count()).thenReturn(1L);

        regionSeedConfig.regionSeedRunner(regionRepository).run();

        verify(regionRepository, never()).save(any(Region.class));
    }
}
