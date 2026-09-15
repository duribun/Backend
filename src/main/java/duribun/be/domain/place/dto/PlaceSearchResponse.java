package duribun.be.domain.place.dto;

import duribun.be.domain.map.dto.TourApiItemResponse;

public record PlaceSearchResponse(
        String placeName,
        String address,
        Double latitude,
        Double longitude
) {

    public static PlaceSearchResponse from(TourApiItemResponse item) {
        return new PlaceSearchResponse(item.title(), item.address(), parseDouble(item.latitude()),
                parseDouble(item.longitude()));
    }

    private static Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Double.parseDouble(value);
    }
}
