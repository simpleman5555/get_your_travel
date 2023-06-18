$(function () {

    changeColorOfActiveMenuItem();

    highlightActualCategoryOnWebshopAdminPage();

    // image popup for smaller city images in Admin section
    $(document).on("click", ".table #cityImageSmall", function (e) {
        e.preventDefault();
        let src = $(this).attr("src");
        let cityName = $(this).attr("cityName");
        $("#imageModal #cityImageLarge").attr("src", src);
        $("#imageModal #imageModalTitle").html(cityName);
        $("#imageModal").modal();
    });

    // image popup for smaller product images in Admin section
    $(document).on("click", "#productImageSmall", function (e) {
        e.preventDefault();
        let productName = $(this).attr("productName");
        let src = $(this).attr("src");
        $("#imageModalTitle").html(productName);
        $("#productImageLarge").attr("src", src);
        $("#imageModal").modal();
    });

    // handling the 'Filter' button on Cities page (filter part)
    $(document).on("click", "#filterCitiesBtn, .paginationBtnForCities", function (e) {
        e.preventDefault();
        let href;
        let cityId = $("#selectCity option:selected").val();
        let countryId = $("#selectCountry option:selected").val();
        let continentId = $("#selectContinent option:selected").val();
        let languageId = $("#selectLanguage option:selected").val();
        if ($(this).attr("href") == undefined) {
            href = "/cities/page/1?cityId=" + cityId + "&countryId=" + countryId + "&continentId=" + continentId + "&languageId=" + languageId + "#filterCities";
        } else {
            let pageNumber = $(this).attr("href").split("/")[3];
            href = "/cities/page/" + pageNumber + "?cityId=" + cityId + "&countryId=" + countryId + "&continentId=" + continentId + "&languageId=" + languageId + "#filterCities";
        }
        $.ajax({
            type: 'get',
            url: href,
            success: function (response) {
                $("#tourists").show();
                $("#documents").show();
                $("#filterCitiesSection").html(response);
                if ($("#citycards article").html() == undefined) {
                    $("#tourists").hide();
                    $("#documents").hide();
                    $("#citycards").html("<h2 style='color: rgba(0,0,0,.5);' class='p-5'>The filter conditions didn't return any results.</h2>");
                }
            },
            error: function (response) {
                $("#tourists").hide();
                $("#documents").hide();
                $("#citycards").html("<h2 style='color: rgba(0,0,0,.5);'>Something went wrong...</h2>");
            }
        });
    });

    // handling the 'Clear' button on Cities page (filter part)
    $(document).on("click", "#clearFilterCitiesBtn", function (e) {
        $("#selectCity").innerHTML = "City";
        $("#selectCountry").innerHTML = "Country";
        $("#selectContinent").innerHTML = "Continent";
        $("#selectLanguage").innerHTML = "Language";

        $("#selectCity option").prop("selected", function () {
            return this.defaultSelected;
        });
        $("#selectCountry option").prop("selected", function () {
            return this.defaultSelected;
        });
        $("#selectContinent option").prop("selected", function () {
            return this.defaultSelected;
        });
        $("#selectLanguage option").prop("selected", function () {
            return this.defaultSelected;
        });
    });

    // handle selected product amount on Webshop page
    $(document).on("change", "#productElementsPerPage", function() {
        let productElementsPerPage = $(this).find(":selected").val();
        let pathnameArr = window.location.pathname.split("/");
        let category = pathnameArr[2];
        if (category == undefined) {
            category = "all";
        }
        let index = pathnameArr.length-1;
        let pageNumber = pathnameArr[index];
        let filter = /^[0-9]$/;
        if (!filter.test(pageNumber)) {
            pageNumber = 1;
        }
        let href = "/products/" + category + "?pageNumber=" + pageNumber + "&productElementsPerPage=" + productElementsPerPage;
        $.ajax({
            type: 'get',
            url: href,
            success: function (response) {
                $("#productElements").html(response);
            },
            error: function (response) {}
        });
    });

    // handling 'Add to cart' button on Webshop page
    $(document).on("click", "a.addToCart", function (e) {
        e.preventDefault();
        let $this = $(this);
        let id = $this.attr("data-id");
        let url = "/products/cart/add/" + id;
        $this.next().removeClass("d-none");

        $.get(url, {}, function (data) {
            $("div.cart").html(data);
        }).done(function () {
            $this.next().addClass("d-none");
            $this.parent().parent().find("div.productAdded").fadeIn();
            setTimeout(() => {
                $this.parent().parent().find("div.productAdded").fadeOut();
            }, 1000);
        });
    });

    // hide message about successful checkout on Webshop page
    if ( $("#checkoutOfCartMessage").html != undefined) {
        setTimeout(() => {
            $("#checkoutOfCartMessage").hide();
        }, 4500);
    }

    // hide message about successful message sending on Contact page
    if ( $("#contactMessage").html != undefined) {
        setTimeout(() => {
            $("#contactMessage").hide();
        }, 4500);
    }

    // hide message about successful city addition on Add a city page in the Admin section
    if ( $("#cityMessage").html != undefined) {
        setTimeout(() => {
            $("#cityMessage").hide();
        }, 4500);
    }

    // hide message about successful category addition on Add a category page in the Admin section
    if ( $("#categoryMessage").html != undefined) {
        setTimeout(() => {
            $("#categoryMessage").hide();
        }, 4500);
    }

    // confirmation of City delete in Admin section
    $(document).on("click", ".table #confirmCityDeletion", function (e) {
        if (! confirm("Are you sure you want to delete?")) {
            return false;
        } else {
            let id = $(this).attr("href").split("/")[4];
            let pageNumber = window.location.pathname.split("/")[4];
            if (pageNumber == undefined) {
                pageNumber = 1;
            }
            if (window.location.search != "" && window.location.search.split("&").length > 1) {
                let queryPieces = window.location.search.split("&");
                field = queryPieces[0].substring(7);
                let queryPiece = queryPieces[1];
                let sortDirectionPiece = queryPiece[queryPiece.length - 3];
                if (sortDirectionPiece == "e") {
                    sortDirection = "desc";
                } else {
                    sortDirection = "asc";
                }
            } else {
                field = "sorting";
                sortDirection = "asc";
            }
            let url = "/admin/cities/delete/" + id + "/page/" + pageNumber + "?field=" + field + "&sortDir=" + sortDirection;
            $(this).attr("href", url);
        }
    });

    // confirmation of Product delete in Admin section
    $(document).on("click", ".table #confirmProductDeletion", function (e) {
        if (! confirm("Are you sure you want to delete?")) {
            return false;
        } else {
            let id = $(this).attr("href").split("/")[4];
            let pageNumber = window.location.pathname.split("/")[5];
            if (pageNumber == undefined) {
                pageNumber = 1;
            }
            let category = window.location.pathname.split("/")[3];
            if (category == undefined) {
                category = "all";
            }
            if (window.location.search != "" && window.location.search.split("&").length > 1) {
                let queryPieces = window.location.search.split("&");
                field = queryPieces[0].substring(7);
                let queryPiece = queryPieces[1];
                let sortDirectionPiece = queryPiece[queryPiece.length - 3];
                if (sortDirectionPiece == "e") {
                    sortDirection = "desc";
                } else {
                    sortDirection = "asc";
                }
            } else {
                field = "sorting";
                sortDirection = "asc";
            }
            if (category != "all" && category != undefined) {
                alert("Deletion is allowed just if All Products category is selected! Please click on All Products category before you would like to delete products!");
                let href = "/admin/products/" + category + "/page/" + pageNumber + "?field=sorting" + "&sortDir=" + sortDirection;
                $(this).attr("href", href);
            } else {
                let url = "/admin/products/delete/" + id + "/page/" + pageNumber + "?field=" + field + "&sortDir=" + sortDirection + "&category=" + category;
                $(this).attr("href", url);
            }
        }
    });

    // confirmation of Category delete in Admin section
    $(document).on("click", ".table #confirmCategoryDeletion", function (e) {
        if (! confirm("Are you sure you want to delete?")) {
            return false;
        } else {
            let id = $(this).attr("href").split("/")[4];
            let pageNumber = window.location.pathname.split("/")[4];
            if (pageNumber == undefined) {
                pageNumber = 1;
            }
            if (window.location.search != "" && window.location.search.split("&").length > 1) {
                let queryPieces = window.location.search.split("&");
                field = queryPieces[0].substring(7);
                let queryPiece = queryPieces[1];
                let sortDirectionPiece = queryPiece[queryPiece.length - 3];
                if (sortDirectionPiece == "e") {
                    sortDirection = "desc";
                } else {
                    sortDirection = "asc";
                }
            } else {
                field = "sorting";
                sortDirection = "asc";
            }
            let url = "/admin/categories/delete/" + id + "/page/" + pageNumber + "?field=" + field + "&sortDir=" + sortDirection;
            $(this).attr("href", url);
        }
    });

    // we set the header of the ajax request with the value of _csrf and _csrf_header meta elements
    let token = $("meta[name='_csrf']").attr("content");
    let header = $("meta[name='_csrf_header']").attr("content");
    $(document).ajaxSend(function (e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });

    // sorting cities in Admin section
    $("table#cities tbody").sortable({
        items: "tr",
        placeholder: "sortable",
        update: function () {
            let ids = $("table#cities tbody").sortable("serialize");
            let pathnameArr = window.location.pathname.split("/");
            let index = pathnameArr.length-1;
            let pageNumber = pathnameArr[index];
            let filter = /^[0-9]$/;
            if (!filter.test(pageNumber)) {
                pageNumber = 1;
            }
            let field;
            let sortDirection;
            if (window.location.search != "") {
                let queryPieces = window.location.search.split("&");
                field = queryPieces[0].substring(7);
                let queryPiece = queryPieces[1];
                let sortDirectionPiece = queryPiece[queryPiece.length - 3];
                if (sortDirectionPiece == "e") {
                    sortDirection = "desc";
                } else {
                    sortDirection = "asc";
                }
            } else {
                field = "sorting";
                sortDirection = "asc";
            }
            let url = "/admin/cities/reorder/" + pageNumber + "?field=" + field + "&sortDir=" + sortDirection;
            $.post(url, ids, function (data) {
                window.location.replace("/admin/cities/page/" + pageNumber + "?field=sorting" + "&sortDir=" + sortDirection);
            });
        }
    });

    // sorting categories in Admin section
    $("table#categories tbody").sortable({
        items: "tr",
        placeholder: "sortable",
        update: function () {
            let ids = $("table#categories tbody").sortable("serialize");
            let pathnameArr = window.location.pathname.split("/");
            let index = pathnameArr.length-1;
            let pageNumber = pathnameArr[index];
            let filter = /^[0-9]$/;
            if (!filter.test(pageNumber)) {
                pageNumber = 1;
            }
            let field;
            let sortDirection;
            if (window.location.search != "") {
                let queryPieces = window.location.search.split("&");
                field = queryPieces[0].substring(7);
                let queryPiece = queryPieces[1];
                let sortDirectionPiece = queryPiece[queryPiece.length - 3];
                if (sortDirectionPiece == "e") {
                    sortDirection = "desc";
                } else {
                    sortDirection = "asc";
                }
            } else {
                field = "sorting";
                sortDirection = "asc";
            }
            let url = "/admin/categories/reorder/" + pageNumber + "?field=" + field + "&sortDir=" + sortDirection;
            $.post(url, ids, function (data) {
                window.location.replace("/admin/categories/page/" + pageNumber + "?field=sorting" + "&sortDir=" + sortDirection);
            });
        }
    });

    // adding WYSIWYG to textarea of 'Add a city' form
    if ( $("#addCity").length) {
        ClassicEditor
            .create(document.querySelector("#addCity"))
            .catch(error => {
                console.log(error);
            });
    }

    // adding WYSIWYG to textarea of 'Edit city: ...' form
    if ( $("#editCity").length) {
        ClassicEditor
            .create(document.querySelector("#editCity"))
            .catch(error => {
                console.log(error);
            });
    }
});

// adding highlighting (black color) for active menu point
function changeColorOfActiveMenuItem() {
    let actualHref = $(location).attr("href");
    if (actualHref.includes("admin")) $("#menuAdmin").css("color", "rgba(0,0,0,.9)");
    if (actualHref.includes("cities") && ! actualHref.includes("admin")) $("#menuCities").css("color", "rgba(0,0,0,.9)");
    if (actualHref.includes("products") && ! actualHref.includes("admin")) $("#menuWebshop").css("color", "rgba(0,0,0,.9)");
    if (actualHref.includes("contact")) $("#menuContact").css("color", "rgba(0,0,0,.9)");
}

// adding highlighting (black color) for actual category on the Categories part of pages (where it exists)
function highlightActualCategoryOnWebshopAdminPage() {
    if ( $("#productCategoriesList").length) {
        let listItems = $("#productCategoriesList").find(".list-group-item");
        let actualHref = $(location).attr("href");
        listItems.each(function () {
            $(this).attr("style", "background-color:#fff;color:#212529");
        });
        if (! actualHref.includes("all") &&
            ! actualHref.includes("Beaching") &&
            ! actualHref.includes("Travel") &&
            ! actualHref.includes("Hiking") &&
            ! actualHref.includes("Mountaineering") &&
            ! actualHref.includes("Diving")
        ) $("#all").attr("style", "background-color:#212529;color:#fff");
        if (actualHref.includes("all")) $("#all").attr("style", "background-color:#212529;color:#fff");
        if (actualHref.includes("Beaching")) $("#Beaching").attr("style", "background-color:#212529;color:#fff");
        if (actualHref.includes("Travel")) $("#Travel").attr("style", "background-color:#212529;color:#fff");
        if (actualHref.includes("Hiking")) $("#Hiking").attr("style", "background-color:#212529;color:#fff");
        if (actualHref.includes("Mountaineering")) $("#Mountaineering").attr("style", "background-color:#212529;color:#fff");
        if (actualHref.includes("Diving")) $("#Diving").attr("style", "background-color:#212529;color:#fff");
    }
}