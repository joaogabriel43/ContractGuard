// Production environment — built by `ng build` (no --configuration flag needed).
// apiUrl is intentionally empty so HTTP calls are relative to the deployment host.
// To point to an explicit backend URL, set it here or patch via CI before building:
//   sed -i "s|apiUrl: ''|apiUrl: '${VITE_API_URL}'|" src/environments/environment.ts
export const environment = {
  production: true,
  apiUrl: ''
};
