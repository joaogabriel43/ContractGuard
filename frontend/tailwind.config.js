/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        apple: {
          bg:       '#000000',
          surface:  '#1c1c1e',
          surface2: '#2c2c2e',
          border:   '#3a3a3c',
          blue:     '#0a84ff',
          green:    '#30d158',
          yellow:   '#ffd60a',
          red:      '#ff453a',
          text:     '#ffffff',
          muted:    'rgba(235,235,245,0.6)',
          subtle:   'rgba(235,235,245,0.3)',
        },
      },
      fontFamily: {
        sans: ['-apple-system', 'BlinkMacSystemFont', 'SF Pro Display', 'Segoe UI', 'system-ui', 'sans-serif'],
      },
      borderRadius: {
        'apple': '12px',
        'pill':  '20px',
      },
    },
  },
  plugins: [],
}
