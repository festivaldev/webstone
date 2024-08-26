const { nextui } = require('@nextui-org/react');

/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}', './node_modules/@nextui-org/theme/dist/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      screens: {
        '3xl': '1720px',
        '4xl': '2160px',
      },
    },
  },
  darkMode: 'class',
  plugins: [
    nextui({
      themes: {
        dark: {
          colors: {
            focus: 'rgba(0, 0, 0, 0)',
            default: {
              DEFAULT: '#1e293b',
              950: '#f8fafc',
              900: '#f1f5f9',
              800: '#e2e8f0',
              700: '#cbd5e1',
              600: '#94a3b8',
              500: '#64748b',
              400: '#475569',
              300: '#334155',
              200: '#1e293b',
              100: '#0f172a',
              50: '#020617',
            },
            content1: '#0f172a',
          },
        },
      },
    }),
  ],
};
