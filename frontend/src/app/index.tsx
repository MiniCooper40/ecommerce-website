import { BrowserRouter } from "react-router-dom";
import { AppProvider } from "./provider";
import { AppRouter } from "./router";

function App() {
  return (
    <AppProvider>
      <BrowserRouter>
        <AppRouter />
      </BrowserRouter>
    </AppProvider>
  );
}

export default App;
