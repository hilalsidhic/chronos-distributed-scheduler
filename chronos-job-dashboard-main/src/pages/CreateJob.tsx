import { useState } from "react";
import { Layout } from "@/components/Layout";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Switch } from "@/components/ui/switch";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { ArrowLeft, Save } from "lucide-react";
import { authService } from "@/auth/authService";
import { API_BASE_URL } from "@/config";

export default function CreateJob() {
  const navigate = useNavigate();
  const [isRecurring, setIsRecurring] = useState(false);
  const [formData, setFormData] = useState({
    name: "",
    payload: "{}",
    intervalSeconds: 0,
    maxRetry: 3,
    maxExecutionTime: 300,
    nextExecutionTime: "",
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const token = authService.getToken();
    if (!token) {
      toast.error("Please login first");
      navigate("/login");
      return;
    }

    try {
      const parsedPayload = JSON.parse(formData.payload);
      const currentUser = authService.getCurrentUser();
      const createdBy = currentUser ? currentUser.email : "system";

      const requestBody = {
        name: formData.name,
        payload: parsedPayload,
        intervalSeconds: isRecurring ? formData.intervalSeconds : 0,
        maxRetry: formData.maxRetry,
        maxExecutionTime: formData.maxExecutionTime,
        createdBy: createdBy,
        nextExecutionTime: formData.nextExecutionTime || null,
      };

      // 2. Updated Endpoint Construction
      const endpoint = isRecurring
        ? `${API_BASE_URL}/scheduler/jobs/recurring`
        : `${API_BASE_URL}/scheduler/jobs`;

      const res = await fetch(endpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": token,
        },
        body: JSON.stringify(requestBody),
      });

      if (res.status === 401) {
          authService.logout();
          navigate("/login");
          throw new Error("Session expired");
      }

      if (!res.ok) throw new Error("Failed to create job");

      toast.success("Job created successfully");
      navigate("/jobs");
    } catch (error) {
      if (error instanceof SyntaxError) {
        toast.error("Invalid JSON payload");
      } else if (error instanceof Error && error.message !== "Session expired") {
        toast.error("Failed to create job");
      }
      console.error(error);
    }
  };

  return (
    <Layout>
      <div className="max-w-3xl space-y-6 animate-in fade-in duration-500">
        <div>
          <Button
            variant="ghost"
            onClick={() => navigate("/jobs")}
            className="mb-4"
          >
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Jobs
          </Button>
          <h1 className="text-4xl font-bold font-heading text-foreground">
            Create New Job
          </h1>
          <p className="text-muted-foreground mt-2">
            Schedule a new one-time or recurring job
          </p>
        </div>

        <form onSubmit={handleSubmit}>
          <Card>
            <CardHeader>
              <CardTitle className="font-heading">Job Configuration</CardTitle>
              <CardDescription>
                Configure the job details and execution parameters
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="space-y-2">
                <Label htmlFor="name">Job Name *</Label>
                <Input
                  id="name"
                  placeholder="e.g., EmailCampaignJob"
                  value={formData.name}
                  onChange={(e) =>
                    setFormData({ ...formData, name: e.target.value })
                  }
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="payload">Payload (JSON) *</Label>
                <Textarea
                  id="payload"
                  placeholder='{"key": "value"}'
                  value={formData.payload}
                  onChange={(e) =>
                    setFormData({ ...formData, payload: e.target.value })
                  }
                  className="font-mono text-sm min-h-32"
                  required
                />
                <p className="text-xs text-muted-foreground">
                  Provide the job payload as a JSON object
                </p>
              </div>

              <div className="flex items-center justify-between p-4 border border-border rounded-lg">
                <div className="space-y-0.5">
                  <Label htmlFor="recurring">Recurring Job</Label>
                  <p className="text-sm text-muted-foreground">
                    Enable to create a job that runs repeatedly
                  </p>
                </div>
                <Switch
                  id="recurring"
                  checked={isRecurring}
                  onCheckedChange={setIsRecurring}
                />
              </div>

              {isRecurring && (
                <div className="space-y-2">
                  <Label htmlFor="interval">Interval (seconds) *</Label>
                  <Input
                    id="interval"
                    type="number"
                    min="1"
                    placeholder="3600"
                    value={formData.intervalSeconds}
                    onChange={(e) =>
                      setFormData({
                        ...formData,
                        intervalSeconds: parseInt(e.target.value) || 0,
                      })
                    }
                    required={isRecurring}
                  />
                  <p className="text-xs text-muted-foreground">
                    Time between job executions (e.g., 3600 = 1 hour)
                  </p>
                </div>
              )}

              <div className="space-y-2">
                <Label htmlFor="nextExecution">Next Execution Time (Optional)</Label>
                <Input
                  id="nextExecution"
                  type="datetime-local"
                  value={formData.nextExecutionTime}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      nextExecutionTime: e.target.value,
                    })
                  }
                />
                <p className="text-xs text-muted-foreground">
                  If not set, the job will start immediately
                </p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="maxRetry">Max Retry Attempts</Label>
                <Input
                  id="maxRetry"
                  type="number"
                  min="0"
                  value={formData.maxRetry}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      maxRetry: parseInt(e.target.value) || 0,
                    })
                  }
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="maxExecutionTime">
                  Max Execution Time (seconds)
                </Label>
                <Input
                  id="maxExecutionTime"
                  type="number"
                  min="1"
                  value={formData.maxExecutionTime}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      maxExecutionTime: parseInt(e.target.value) || 1,
                    })
                  }
                />
              </div>

              <div className="flex gap-4 pt-4">
                <Button type="submit" size="lg" className="flex-1">
                  <Save className="h-5 w-5 mr-2" />
                  Create Job
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => navigate("/jobs")}
                >
                  Cancel
                </Button>
              </div>
            </CardContent>
          </Card>
        </form>
      </div>
    </Layout>
  );
}