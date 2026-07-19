export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  path?: string;
  instance?: string;
  correlationId?: string;
  timestamp?: string;
  errors?: Record<string, string>;
}

export class ApiProblemError extends Error {
  readonly problem: ProblemDetail;

  constructor(problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? "Request failed");
    this.name = "ApiProblemError";
    this.problem = problem;
  }
}
