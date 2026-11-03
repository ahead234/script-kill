document.addEventListener('DOMContentLoaded', function () {
    var toggle = document.querySelector('[data-nav-toggle]');
    var nav = document.querySelector('[data-nav]');
    if (toggle && nav) {
        toggle.addEventListener('click', function () {
            nav.classList.toggle('is-open');
        });
    }

    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!window.confirm(form.getAttribute('data-confirm'))) event.preventDefault();
        });
    });

    document.querySelectorAll('[data-reason-form]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            var input = form.querySelector('input[name="reason"]');
            if (input && !input.value.trim()) {
                event.preventDefault();
                input.focus();
            }
        });
    });

    var slider = document.querySelector('[data-slider]');
    if (slider) {
        var slides = slider.querySelectorAll('.slide');
        var dots = slider.querySelectorAll('.dot');
        var current = 0;
        var timer = null;

        function show(next) {
            current = (next + slides.length) % slides.length;
            Array.prototype.forEach.call(slides, function (slide, i) {
                slide.classList.toggle('is-active', i === current);
            });
            Array.prototype.forEach.call(dots, function (dot, i) {
                dot.classList.toggle('is-active', i === current);
            });
        }

        function stop() {
            if (timer) {
                window.clearInterval(timer);
                timer = null;
            }
        }

        function start() {
            var interval = parseInt(slider.getAttribute('data-interval'), 10) || 4500;
            stop();
            timer = window.setInterval(function () { show(current + 1); }, interval);
        }

        Array.prototype.forEach.call(dots, function (dot) {
            dot.addEventListener('click', function () {
                show(parseInt(dot.getAttribute('data-slide-to'), 10) || 0);
                start();
            });
        });
        slider.addEventListener('mouseenter', stop);
        slider.addEventListener('mouseleave', start);
        start();
    }
});
