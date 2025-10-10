(function ($) {
    "use strict";

    /*==================================================================
    [ Load page ]
    ==================================================================*/
    $(".animsition").animsition({
        inClass: 'fade-in',
        outClass: 'fade-out',
        inDuration: 1500,
        outDuration: 800,
        linkElement: '.animsition-link',
        loading: true,
        loadingParentElement: 'html',
        loadingClass: 'animsition-loading-1',
        loadingInner: '<div class="loader05"></div>',
        timeout: false,
        timeoutCountdown: 5000,
        onLoadEvent: true,
        browser: ['animation-duration', '-webkit-animation-duration'],
        overlay: false,
        overlayClass: 'animsition-overlay-slide',
        overlayParentElement: 'html',
        transition: function (url) { window.location.href = url; }
    });

    /*==================================================================
    [ Back to top ]
    ==================================================================*/
    var windowH = $(window).height() / 2;
    $(window).on('scroll', function () {
        if ($(this).scrollTop() > windowH) {
            $("#myBtn").css('display', 'flex');
        } else {
            $("#myBtn").css('display', 'none');
        }
    });
    $('#myBtn').on("click", function () {
        $('html, body').animate({ scrollTop: 0 }, 300);
    });

    /*==================================================================
    [ Fixed Header ]
    ==================================================================*/
    var headerDesktop = $('.container-menu-desktop');
    var wrapMenu = $('.wrap-menu-desktop');
    var posWrapHeader = $('.top-bar').length > 0 ? $('.top-bar').height() : 0;

    if ($(window).scrollTop() > posWrapHeader) {
        $(headerDesktop).addClass('fix-menu-desktop');
        $(wrapMenu).css('top', 0);
    } else {
        $(headerDesktop).removeClass('fix-menu-desktop');
        $(wrapMenu).css('top', posWrapHeader - $(this).scrollTop());
    }

    $(window).on('scroll', function () {
        if ($(this).scrollTop() > posWrapHeader) {
            $(headerDesktop).addClass('fix-menu-desktop');
            $(wrapMenu).css('top', 0);
        } else {
            $(headerDesktop).removeClass('fix-menu-desktop');
            $(wrapMenu).css('top', posWrapHeader - $(this).scrollTop());
        }
    });

    /*==================================================================
    [ Menu mobile ]
    ==================================================================*/
    $('.btn-show-menu-mobile').on('click', function () {
        $(this).toggleClass('is-active');
        $('.menu-mobile').slideToggle();
    });

    var arrowMainMenu = $('.arrow-main-menu-m');
    arrowMainMenu.each(function () {
        $(this).on('click', function () {
            $(this).parent().find('.sub-menu-m').slideToggle();
            $(this).toggleClass('turn-arrow-main-menu-m');
        });
    });

    $(window).resize(function () {
        if ($(window).width() >= 992) {
            if ($('.menu-mobile').css('display') == 'block') {
                $('.menu-mobile').css('display', 'none');
                $('.btn-show-menu-mobile').toggleClass('is-active');
            }

            $('.sub-menu-m').each(function () {
                if ($(this).css('display') == 'block') {
                    $(this).css('display', 'none');
                    $(arrowMainMenu).removeClass('turn-arrow-main-menu-m');
                }
            });
        }
    });

    /*==================================================================
    [ Show / hide modal search ]
    ==================================================================*/
    $('.js-show-modal-search').on('click', function () {
        $('.modal-search-header').addClass('show-modal-search');
        $(this).css('opacity', '0');
    });

    $('.js-hide-modal-search').on('click', function () {
        $('.modal-search-header').removeClass('show-modal-search');
        $('.js-show-modal-search').css('opacity', '1');
    });

    $('.container-search-header').on('click', function (e) {
        e.stopPropagation();
    });

    /*==================================================================
    [ Quantity +/- on product detail ]
    ==================================================================*/
    try {
        $('.wrap-num-product').each(function () {
            var $wrap = $(this);
            var $input = $wrap.find('input.num-product');

            // Increase
            $wrap.find('.btn-num-product-up')
                .off('click.qty')
                .on('click.qty', function (e) {
                    e.preventDefault();
                    var v = parseInt($input.val(), 10);
                    if (isNaN(v)) v = 0;
                    $input.val(v + 1).trigger('change');
                });

            // Decrease (min = 1)
            $wrap.find('.btn-num-product-down')
                .off('click.qty')
                .on('click.qty', function (e) {
                    e.preventDefault();
                    var v = parseInt($input.val(), 10);
                    if (isNaN(v) || v <= 1) {
                        $input.val(1).trigger('change');
                        return;
                    }
                    $input.val(v - 1).trigger('change');
                });
        });
    } catch (e) { }

    /*==================================================================
    [ Cart helpers ]
    ==================================================================*/
    function formatCurrencyVND(n) {
        try { return (Number(n) || 0).toLocaleString('vi-VN') + '₫'; } catch (e) { return n; }
    }

    function updateCartBadge(totalQty) {
        try {
            console.log('[Cart] updateCartBadge called with totalQty=', totalQty);
            var $badge = $('.icon-header-noti.js-show-cart');
            if ($badge && $badge.length) {
                var v = Number(totalQty || 0);
                $badge.attr('data-notify', v);
                $badge.show(); // luôn hiển thị, kể cả =0
                console.log('[Cart] badge updated on', $badge.get(0));
            } else {
                console.warn('[Cart] badge element not found: .icon-header-noti.js-show-cart');
            }
        } catch (e) { }
    }

    function renderHeaderCart(items) {
        try {
            console.log('[Cart] renderHeaderCart items=', items);
            var $list = $('#header-cart-list');
            var $total = $('#header-cart-total');
            if (!$list.length || !$total.length) return;

            $list.empty();
            var totalPrice = 0;

            if (Array.isArray(items) && items.length) {
                items.forEach(function (it) {
                    var p = (it && it.product) ? it.product : {};
                    var qty = Number(it.quantity || 0);
                    var price = Number(p.price || 0);
                    totalPrice += qty * price;

                    var img = (p.firstImageFileName)
                        ? ('/images/' + p.firstImageFileName)
                        : '/images/product-01.jpg';

                    var name = p.name || 'Sản phẩm';
                    var li = `
                        <li class="header-cart-item flex-w flex-t m-b-12">
                            <div class="header-cart-item-img">
                                <img src="${img}" alt="IMG">
                            </div>
                            <div class="header-cart-item-txt p-t-8">
                                <a href="#" class="header-cart-item-name m-b-18 hov-cl1 trans-04">${name}</a>
                                <span class="header-cart-item-info">${qty} x ${price.toLocaleString()}đ</span>
                            </div>
                        </li>`;
                    $list.append(li);
                });
            } else {
                $list.append('<li class="p-2 text-muted">Giỏ hàng trống</li>');
            }

            $total.text('Total: ' + totalPrice.toLocaleString() + 'đ');
            console.log('[Cart] renderHeaderCart done. totalPrice=', totalPrice);
        } catch (e) { }
    }

    function loadCart() {
        try {
            console.log('[Cart] loadCart start /cart/items');
            $.ajax({
                url: '/cart/items',
                method: 'GET',
                dataType: 'text' // tránh parsererror khi JSON quá sâu/đệ quy
            })
                .done(function (resp) {
                    var items = [];
                    try {
                        items = typeof resp === 'string' ? JSON.parse(resp) : resp;
                    } catch (err) {
                        console.error('[Cart] parse error:', err);
                        items = [];
                    }
                    console.log('[Cart] loadCart success items=', items);
                    renderHeaderCart(items);
                    var totalQty = 0;
                    if (Array.isArray(items)) {
                        items.forEach(function (it) { totalQty += Number(it.quantity || 0); });
                    }
                    updateCartBadge(totalQty);
                })
                .fail(function (xhr) {
                    console.error('[Cart] loadCart fail status=', xhr && xhr.status, 'response=', xhr && xhr.responseText);
                    if (xhr && xhr.status === 401) {
                        renderHeaderCart([]);
                        updateCartBadge(0);
                    }
                });
        } catch (e) { }
    }

    $('.js-addcart-detail').each(function () {
        var $btn = $(this);
        var nameProduct = $btn.closest('.product-detail').find('.js-name-detail').text() || 'Sản phẩm';
        $btn.on('click', function () {
            try { swal(nameProduct, 'đã được thêm vào giỏ hàng!', 'success'); } catch (e) { }

            var productId = $btn.data('product-id');
            var qty = 1;
            var $qtyInput = $btn.closest('.size-204').find('input.num-product');
            if ($qtyInput && $qtyInput.length) {
                var v = Number($qtyInput.val());
                if (!isNaN(v) && v > 0) qty = v;
            }

            if (!productId) return;

            $.ajax({
                url: '/cart/add',
                method: 'POST',
                data: { productId: productId, quantity: qty }
            })
                .done(function (totalQty) {
                    updateCartBadge(totalQty);
                    loadCart();
                })
                .fail(function (xhr) {
                    if (xhr.status === 401) {
                        alert('Bạn cần đăng nhập để thêm vào giỏ hàng!');
                    }
                });
        });
    });

    // Đồng bộ badge khi load trang
    try { loadCart(); } catch (e) { }

    /*==================================================================
    [ Header cart open/close ]
    ==================================================================*/
    try {
        var $panelCart = $('.js-panel-cart');
        var $showCartBtn = $('.js-show-cart');
        var $hideCartBtn = $('.js-hide-cart, .s-full');

        function openCartPanel() {
            console.log('[Cart] openCartPanel');
            if ($panelCart.length) {
                $panelCart.addClass('show-header-cart');
                $('body').addClass('show-header-cart');
                console.log('[Cart] panel classes=', $panelCart.get(0) && $panelCart.get(0).className);
                loadCart();
            }
        }

        function closeCartPanel() {
            console.log('[Cart] closeCartPanel');
            if ($panelCart.length) {
                $panelCart.removeClass('show-header-cart');
                $('body').removeClass('show-header-cart');
                console.log('[Cart] panel classes=', $panelCart.get(0) && $panelCart.get(0).className);
            }
        }

        console.log('[Cart] init header cart: panel=', $panelCart.length, 'showBtn=', $showCartBtn.length, 'hideBtn=', $hideCartBtn.length);

        if ($showCartBtn.length) {
            $showCartBtn.off('click.headerCart').on('click.headerCart', function (e) {
                e.preventDefault();
                console.log('[Cart] showCartBtn clicked');
                // toggle
                if ($panelCart.hasClass('show-header-cart')) {
                    closeCartPanel();
                } else {
                    openCartPanel();
                }
            });
        }
        if ($hideCartBtn.length) {
            $hideCartBtn.off('click.headerCart').on('click.headerCart', function (e) {
                e.preventDefault();
                console.log('[Cart] hideCartBtn clicked');
                closeCartPanel();
            });
        }

        // Loại bỏ delegated fallback để tránh double toggle gây đóng ngay sau khi mở
    } catch (e) { }

    /*==================================================================
    [ Isotope filter init for categories ]
    ==================================================================*/
    try {
        var $grid = $('.isotope-grid');
        if ($grid.length && typeof $.fn.isotope === 'function') {
            console.log('[Isotope] init grids count=', $grid.length);
            $grid.each(function () {
                var $g = $(this);
                $g.isotope({
                    itemSelector: '.isotope-item',
                    layoutMode: 'fitRows'
                });
            });

            var $filterBtns = $('.filter-tope-group [data-filter]');
            $filterBtns.off('click.isotope').on('click.isotope', function (e) {
                e.preventDefault();
                console.log('[Isotope] filter clicked value=', $(this).attr('data-filter'));
                var filter = $(this).attr('data-filter') || '*';
                $grid.isotope({ filter: filter });

                // active state
                $(this).parent().find('.how-active1').removeClass('how-active1');
                $(this).addClass('how-active1');
            });
        } else {
            if (!$grid.length) console.warn('[Isotope] grid not found (.isotope-grid)');
            if (typeof $.fn.isotope !== 'function') console.warn('[Isotope] isotope plugin is not loaded');
        }
    } catch (e) { }

})(jQuery);
