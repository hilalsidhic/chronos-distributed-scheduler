import { Link, useLocation } from "react-router-dom";
import { 
  LayoutDashboard, 
  Clock, 
  Plus, 
  Activity 
} from "lucide-react";
import { cn } from "@/lib/utils";

const navigation = [
  { name: "Dashboard", href: "/", icon: LayoutDashboard },
  { name: "Jobs", href: "/jobs", icon: Clock },
  { name: "Create Job", href: "/jobs/new", icon: Plus },
  { name: "Executions", href: "/executions", icon: Activity },
];

interface LayoutProps {
  children: React.ReactNode;
}

export function Layout({ children }: LayoutProps) {
  const location = useLocation();

  return (
    <div className="flex min-h-screen bg-background">
      {/* Sidebar */}
      <aside className="w-64 border-r border-border bg-sidebar">
        <div className="flex h-16 items-center px-6 border-b border-sidebar-border">
          <Link to="/" className="flex items-center space-x-2">
            <Clock className="h-6 w-6 text-primary" />
            <span className="font-heading font-bold text-lg text-sidebar-foreground">
              Chronos
            </span>
          </Link>
        </div>
        <nav className="flex flex-col gap-1 p-4">
          {navigation.map((item) => {
            const isActive = location.pathname === item.href;
            return (
              <Link
                key={item.name}
                to={item.href}
                className={cn(
                  "flex items-center gap-3 px-4 py-3 text-sm font-medium rounded-lg transition-all duration-200",
                  isActive
                    ? "bg-sidebar-accent text-sidebar-primary"
                    : "text-sidebar-foreground hover:bg-sidebar-accent/50"
                )}
              >
                <item.icon className="h-5 w-5" />
                {item.name}
              </Link>
            );
          })}
        </nav>
      </aside>

      {/* Main content */}
      <main className="flex-1">
        <div className="h-full p-8">
          {children}
        </div>
      </main>
    </div>
  );
}
