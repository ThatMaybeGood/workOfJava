/**
 * Login page — particle engine + auth gate
 */
(function() {
    const canvas = document.getElementById('particleCanvas');
    const ctx = canvas.getContext('2d');
    const form = document.getElementById('loginForm');
    const card = document.getElementById('loginCard');
    const btn = document.getElementById('loginBtn');
    const errorEl = document.getElementById('loginError');

    /* ====== Particle Engine ====== */

    let particles = [];
    let mouseX = -1000;
    let mouseY = -1000;

    function resize() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
    }

    function createParticles(count) {
        particles = [];
        for (let i = 0; i < count; i++) {
            particles.push({
                x: Math.random() * canvas.width,
                y: Math.random() * canvas.height,
                r: Math.random() * 2.5 + 1.5,
                dx: (Math.random() - 0.5) * 0.6,
                dy: (Math.random() - 0.5) * 0.6,
                opacity: Math.random() * 0.3 + 0.08,
                hue: [220, 240, 260][Math.floor(Math.random() * 3)] + Math.random() * 20
            });
        }
    }

    function drawParticles() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        particles.forEach(p => {
            // Soft radial gradient for each particle
            const gradient = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, p.r * 3);
            gradient.addColorStop(0, `hsla(${p.hue}, 80%, 70%, ${p.opacity})`);
            gradient.addColorStop(0.5, `hsla(${p.hue}, 80%, 65%, ${p.opacity * 0.4})`);
            gradient.addColorStop(1, 'transparent');

            ctx.beginPath();
            ctx.arc(p.x, p.y, p.r * 3, 0, Math.PI * 2);
            ctx.fillStyle = gradient;
            ctx.fill();
        });

        // Draw constellation lines between nearby particles
        for (let i = 0; i < particles.length; i++) {
            for (let j = i + 1; j < particles.length; j++) {
                const dx = particles[i].x - particles[j].x;
                const dy = particles[i].y - particles[j].y;
                const dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < 140) {
                    const alpha = (1 - dist / 140) * 0.12;
                    ctx.beginPath();
                    ctx.moveTo(particles[i].x, particles[i].y);
                    ctx.lineTo(particles[j].x, particles[j].y);
                    ctx.strokeStyle = `rgba(129, 140, 248, ${alpha})`;
                    ctx.lineWidth = 0.6;
                    ctx.stroke();
                }
            }
        }
    }

    function updateParticles() {
        particles.forEach(p => {
            p.x += p.dx;
            p.y += p.dy;

            // Slight attraction toward mouse
            const mdx = mouseX - p.x;
            const mdy = mouseY - p.y;
            const mdist = Math.sqrt(mdx * mdx + mdy * mdy);
            if (mdist < 200) {
                const force = (1 - mdist / 200) * 0.02;
                p.dx += mdx * force * 0.01;
                p.dy += mdy * force * 0.01;
            }

            // Damping
            p.dx *= 0.9995;
            p.dy *= 0.9995;

            // Speed clamp
            const speed = Math.sqrt(p.dx * p.dx + p.dy * p.dy);
            if (speed > 0.8) {
                p.dx = (p.dx / speed) * 0.8;
                p.dy = (p.dy / speed) * 0.8;
            }

            // Wrap around edges
            if (p.x < -20) p.x = canvas.width + 20;
            if (p.x > canvas.width + 20) p.x = -20;
            if (p.y < -20) p.y = canvas.height + 20;
            if (p.y > canvas.height + 20) p.y = -20;
        });
    }

    function animate() {
        updateParticles();
        drawParticles();
        requestAnimationFrame(animate);
    }

    document.addEventListener('mousemove', function(e) {
        mouseX = e.clientX;
        mouseY = e.clientY;
    });

    window.addEventListener('resize', function() {
        resize();
        createParticles(70);
    });

    // Start
    resize();
    createParticles(70);
    animate();

    /* ====== Login Form ====== */

    form.addEventListener('submit', async function(e) {
        e.preventDefault();

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value.trim();

        // Clear previous error
        errorEl.textContent = '';
        card.classList.remove('shake');

        if (!username || !password) {
            errorEl.textContent = '请输入用户名和密码';
            card.classList.add('shake');
            return;
        }

        // Loading state
        btn.classList.add('loading');
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> 登录中...';
        btn.disabled = true;

        // Simulate auth delay
        await new Promise(r => setTimeout(r, 800));

        // Accept any non-empty credentials (client-side gate)
        const token = {
            user: username,
            time: Date.now()
        };
        localStorage.setItem('etl-login-token', JSON.stringify(token));

        // Success animation
        btn.classList.remove('loading');
        btn.classList.add('success');
        btn.innerHTML = '<i class="fas fa-check"></i> 登录成功';

        await new Promise(r => setTimeout(r, 500));
        window.location.href = 'index.html';
    });

    // Enter key triggers submit
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Enter') {
            form.dispatchEvent(new Event('submit'));
        }
    });
})();
