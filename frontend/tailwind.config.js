/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        slate: require('tailwindcss/colors').slate,
        indigo: require('tailwindcss/colors').indigo,
      }
    },
  },
  plugins: [],
}
