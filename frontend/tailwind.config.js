/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        // "Box office at dusk" palette - deep charcoal-plum background
        // with two warm accents (marquee gold + velvet crimson) rather
        // than a single neon-on-black accent.
        ink: {
          DEFAULT: '#191521',
          soft: '#211C2B',
          softer: '#2A2436',
          line: '#3A3348',
        },
        paper: {
          DEFAULT: '#F1ECE3',
          dim: '#B7AFC4',
          faint: '#7C7391',
        },
        marquee: {
          DEFAULT: '#E8A93B',
          bright: '#F5C463',
          dim: '#A97C2C',
        },
        velvet: {
          DEFAULT: '#8B2635',
          bright: '#B23347',
        },
        slate: {
          seat: '#5B5568',
        },
      },
      fontFamily: {
        display: ['"Bebas Neue"', 'sans-serif'],
        body: ['"Inter"', 'system-ui', 'sans-serif'],
        mono: ['"IBM Plex Mono"', 'monospace'],
      },
      letterSpacing: {
        marquee: '0.08em',
      },
      boxShadow: {
        glow: '0 0 0 1px rgba(232,169,59,0.35), 0 0 24px rgba(232,169,59,0.15)',
      },
    },
  },
  plugins: [],
};
