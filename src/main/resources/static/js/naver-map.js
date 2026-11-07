"use strict";

(function () {
    const SHOP_ADDRESS = "서울특별시 구로구 고척로 31";

    function setMessage(message) {
        const element = document.getElementById("map-message");

        if (element) {
            element.textContent = message;
        }
    }

    window.navermap_authFailure = function () {
        console.error("[GarageCare Maps] SDK authentication failed");

        setMessage(
            "지도 인증에 실패했습니다. " +
            "네이버 지도에서 위치를 확인해 주세요."
        );
    };

    window.initDongbuMap = function () {
        const container = document.getElementById("naver-map");

        if (!container) {
            return;
        }

        if (!window.naver?.maps?.Service?.geocode) {
            console.error("[GarageCare Maps] Geocoder unavailable");

            setMessage(
                "주소 검색 서비스를 불러오지 못했습니다."
            );
            return;
        }

        naver.maps.Service.geocode(
            { query: SHOP_ADDRESS },
            function (status, response) {

                if (status !== naver.maps.Service.Status.OK) {
                    console.error(
                        "[GarageCare Maps] Geocoding request failed",
                        { status, response }
                    );

                    setMessage(
                        "주소를 조회하지 못했습니다. " +
                        "네이버 지도에서 위치를 확인해 주세요."
                    );
                    return;
                }

                const addresses = response?.v2?.addresses;

                if (!addresses || addresses.length === 0) {
                    console.warn(
                        "[GarageCare Maps] No matching addresses",
                        response
                    );

                    setMessage(
                        "주소 검색 결과가 없습니다. " +
                        "네이버 지도에서 위치를 확인해 주세요."
                    );
                    return;
                }

                const latitude = Number(addresses[0].y);
                const longitude = Number(addresses[0].x);

                if (
                    !Number.isFinite(latitude) ||
                    !Number.isFinite(longitude)
                ) {
                    setMessage("유효한 지도 좌표를 확인하지 못했습니다.");
                    return;
                }

                const position = new naver.maps.LatLng(
                    latitude,
                    longitude
                );

                try {
                    const map = new naver.maps.Map("naver-map", {
                        center: position,
                        zoom: 17,
                        zoomControl: true,
                        zoomControlOptions: {
                            position: naver.maps.Position.TOP_RIGHT
                        }
                    });

                    new naver.maps.Marker({
                        map: map,
                        position: position,
                        title: "동부자동차공업사"
                    });

                    setMessage(
                        "동부자동차공업사 위치입니다. " +
                        "방문 전 정확한 진입 위치를 확인해 주세요."
                    );

                } catch (error) {
                    console.error(
                        "[GarageCare Maps] Rendering failed",
                        error
                    );

                    setMessage(
                        "지도를 표시하는 중 오류가 발생했습니다."
                    );
                }
            }
        );
    };
})();